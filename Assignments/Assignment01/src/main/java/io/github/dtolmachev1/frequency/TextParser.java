package io.github.dtolmachev1.frequency;

import io.github.dtolmachev1.dictionary.Dictionary;
import io.github.dtolmachev1.dictionary.Lemma;
import io.github.dtolmachev1.dictionary.OpencorporaDictionary;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class TextParser {
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final String FREQUENCY_DICTIONARY = "../../Resources/frequency_dict.txt";
  private static final String OPENCORPORA_DICTIONARY = "../../Resources/dict.opcorpora.xml";
  private static final Map<String, String> toCorpora = Map.ofEntries(
      new SimpleImmutableEntry<>("s", "NOUN"),
      new SimpleImmutableEntry<>("a", "ADJF"),
      new SimpleImmutableEntry<>("v", "VERB"),
      new SimpleImmutableEntry<>("anum", "NUMR"),
      new SimpleImmutableEntry<>("num", "NUMR"),
      new SimpleImmutableEntry<>("adv", "ADVB"),
      new SimpleImmutableEntry<>("advpro", "ADVB"),
      new SimpleImmutableEntry<>("apro", "NPRO"),
      new SimpleImmutableEntry<>("spro", "NPRO"),
      new SimpleImmutableEntry<>("pr", "PREP"),
      new SimpleImmutableEntry<>("conj", "CONJ"),
      new SimpleImmutableEntry<>("part", "PRCL"),
      new SimpleImmutableEntry<>("intj", "INTJ"),
  new SimpleImmutableEntry<>("unknown", "UNKNOWN"));
  private Writer outputWriter;
  private final FrequencyDictionaryParser frequencyDictionaryParser;
  private final Map<WordData, WordDataFrequency> lemmasFrequency;
  private final Dictionary dictionary;
  private int wordCount;
  private int docCount;

  public TextParser(String filename) {
    try {
      this.outputWriter = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(filename)), CHARSET);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    this.frequencyDictionaryParser = new FrequencyDictionaryParser();
    this.frequencyDictionaryParser.parse(FREQUENCY_DICTIONARY);
    this.lemmasFrequency = new HashMap<>();
    this.dictionary = OpencorporaDictionary.load(OPENCORPORA_DICTIONARY);
    this.wordCount = 0;
    this.docCount = 0;
  }

  public void increaseDocCount() {
    this.docCount++;
  }

  public String removeAcutes(String text) {
    return text.replaceAll("\\u0301", "").replaceAll("[\\u00C1\\u00E1]", "\u0430").replaceAll("[\\u00C9\\u00E9]", "\u0435").replaceAll("[\\u00D3\\u00F3]", "\u043E").replaceAll("\\u00FD", "\u0443");
  }

  public List<String> tokenize(String text) {
    List<String> tokens = new ArrayList<>();
    String clearText = removeAcutes(text).toLowerCase();
    Pattern pattern = Pattern.compile("\\p{InCYRILLIC}+(-\\p{InCYRILLIC}+)?");
    Matcher matcher = pattern.matcher(clearText);
    while (matcher.find()) {
      tokens.add(clearText.substring(matcher.start(), matcher.end()));
    }
    return tokens;
  }

  public WordData resolveAmbiguity(List<Lemma> possibleLemmas) {
    String word = null;
    String wordPos = null;
    if (possibleLemmas.size() == 1) {
      Lemma lemma = possibleLemmas.get(0);
      word = lemma.getWord();
      wordPos = lemma.getGrammemes().get(0).getName();
    } else {
      double maxIpm = 0.0;
      for (Lemma possibleLemma : possibleLemmas) {
        String possibleWord = possibleLemma.getWord();
        FrequencyDictionaryData frequencyDictionaryData = this.frequencyDictionaryParser.getFrequencyWordData(possibleWord);
        if (frequencyDictionaryData.ipm() > maxIpm) {
          word = possibleWord;
          wordPos = toCorpora.get(frequencyDictionaryData.pos());
          maxIpm = frequencyDictionaryData.ipm();
        }
      }
      if (maxIpm == 0) {
        word = possibleLemmas.get(0).getWord();
        wordPos = possibleLemmas.get(0).getGrammemes().get(0).getName();
      }
    }
    return new WordData(word, wordPos);
  }

  public void parseOneText(String text) {
    List<String> tokens = this.tokenize(text);
    this.wordCount = tokens.size();
    for (String token : tokens) {
      List<Lemma> possibleLemmas = this.dictionary.getLemmas(token);
      if (Objects.isNull(possibleLemmas)) {
        continue;
      }
      WordData wordData = resolveAmbiguity(possibleLemmas);
      WordDataFrequency lemmaFrequency = this.lemmasFrequency.computeIfAbsent(wordData, k -> new WordDataFrequency());
      lemmaFrequency.increaseEntryCount();
      if (lemmaFrequency.getLastTextId() < this.docCount) {
        lemmaFrequency.setLastTextId(this.docCount);
        lemmaFrequency.increaseTextCount();
      }
    }
  }

  public void print() {
    try {
      this.outputWriter.write("Number of texts in corpus: " + this.docCount + "\n");
      this.outputWriter.write("Unique lemmas found: " + this.lemmasFrequency.size() + "\n\n");
      for (Entry<WordData, WordDataFrequency> entry : this.lemmasFrequency.entrySet().stream().sorted((o1, o2) -> Double.compare(o2.getValue().getEntryCount(), o1.getValue().getEntryCount())).toList()) {
        this.outputWriter.write(String.format("<%s, %s, %.3g, %.3g>\n", entry.getKey().lemma(), entry.getKey().pos(), entry.getValue().getEntryCount() / this.wordCount, entry.getValue().getTextCount() / this.docCount));
      }
    this.outputWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
