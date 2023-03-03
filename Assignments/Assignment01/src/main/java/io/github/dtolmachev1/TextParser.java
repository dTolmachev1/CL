package io.github.dtolmachev1;

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
import java.util.Comparator;
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
      new SimpleImmutableEntry<>("intj", "INTJ"));
  private Writer outputWriter;
  private final FrequencyDictionaryParser frequencyDictionaryParser;
  private final Map<WordData, Double> lemmasFrequency;
  private final Dictionary dictionary;

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
  }

  public List<String> tokenize(String text) {
    List<String> tokens = new ArrayList<>();
    String clearText = text.replaceAll("\\u0301", "").toLowerCase();
    Pattern pattern = Pattern.compile("\\p{InCYRILLIC}+(-\\p{InCYRILLIC}+)?");
    Matcher matcher = pattern.matcher(clearText);
    while (matcher.find()) {
      tokens.add(clearText.substring(matcher.start(), matcher.end()));
    }
    return tokens;
  }

  public void parseOneText(String text) {
    List<String> tokens = this.tokenize(text);
    for (String token : tokens) {
      List<Lemma> possibleLemmas = this.dictionary.getLemmas(token);
      if (Objects.isNull(possibleLemmas)) {
        continue;
      }
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
          FrequencyWordData frequencyWordData = this.frequencyDictionaryParser.getFrequencyWordData(possibleWord);
          if (frequencyWordData.ipm() > maxIpm) {
            word = possibleWord;
            wordPos = toCorpora.get(frequencyWordData.pos());
            maxIpm = frequencyWordData.ipm();
          }
        }
        if (maxIpm == 0) {
          word = possibleLemmas.get(0).getWord();
          wordPos = possibleLemmas.get(0).getGrammemes().get(0).getName();
        }
      }
      WordData wordData = new WordData(word, wordPos);
      this.lemmasFrequency.merge(wordData, 1.0, Double::sum);
    }
  }

  public void print() {
    this.lemmasFrequency.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).forEach(entry -> {
      try {
        this.outputWriter.write("<" + entry.getKey().lemma() + ", " + entry.getKey().pos() + ", " + entry.getValue() + ">\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
