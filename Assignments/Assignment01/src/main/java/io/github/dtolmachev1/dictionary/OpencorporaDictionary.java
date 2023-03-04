package io.github.dtolmachev1.dictionary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

@SuppressWarnings("SpellCheckingInspection")
public class OpencorporaDictionary implements Dictionary {
  private final Trie<String, Grammeme> grammemes;
  private final Trie<String, Set<Lemma>> stems;
  private final Trie<String, Set<Lemma>> suffixes;

  public OpencorporaDictionary() {
    this.grammemes = new PatriciaTrie<>();
    this.stems = new PatriciaTrie<>();
    this.suffixes = new PatriciaTrie<>();
  }

  public static OpencorporaDictionary load(String filename) {
    OpencorporaDictionaryParser opencorporaDictionaryParser = new OpencorporaDictionaryParser();
    return opencorporaDictionaryParser.parse(filename);
  }

  @Override
  public Grammeme getGrammeme(String name) {
    return this.grammemes.get(name);
  }

  public void addGrammeme(Grammeme grammeme) {
    this.grammemes.put(grammeme.getName(), grammeme);
  }

  @Override
  public List<Lemma> getLemmas(String word) {
    Set<Lemma> wordCandidates = new HashSet<>();
    for (int i = 0; i <= word.length() && wordCandidates.isEmpty(); i++) {
      if (this.suffixes.containsKey(new StringBuilder(word.substring(i)).reverse().toString()) && this.stems.containsKey(word.substring(0, i))) {
        wordCandidates.addAll(this.suffixes.get(new StringBuilder(word.substring(i)).reverse().toString()));
        wordCandidates.retainAll(this.stems.get(word.substring(0, i)));
      }
    }
    return !wordCandidates.isEmpty() ? List.copyOf(wordCandidates) : null;
  }

  public void addForm(String stem, String suffix, Lemma lemma) {
    this.stems.computeIfAbsent(stem, k -> new HashSet<>()).add(lemma);
    this.suffixes.computeIfAbsent(new StringBuilder(suffix).reverse().toString(), k -> new HashSet<>()).add(lemma);
  }
}
