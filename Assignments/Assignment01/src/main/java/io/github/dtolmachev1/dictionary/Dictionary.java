package io.github.dtolmachev1.dictionary;

import java.util.List;
import java.util.Map;

@SuppressWarnings("SpellCheckingInspection")
public interface Dictionary {
  static Dictionary load(String filename) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  Grammeme getGrammeme(String name);

  Map<String, List<Form>> getForms(String word);

  List<Lemma> getLemmas(String word);
}
