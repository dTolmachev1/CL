package io.github.dtolmachev1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FrequencyDictionaryParser {
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final int LEMMA_INDEX = 0;
  private static final int POS_INDEX = 1;
  private static final int IPM_INDEX = 2;
  private final Map<String, FrequencyWordData> records;

  public FrequencyDictionaryParser() {
    this.records = new HashMap<>();
  }

  public void parse(String filename) {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename, CHARSET))) {
      String line = bufferedReader.readLine();
      while (Objects.nonNull(line)) {
        String[] values = line.split("\\s");
        this.records.putIfAbsent(values[LEMMA_INDEX], new FrequencyWordData(values[POS_INDEX], Double.parseDouble(values[IPM_INDEX])));
        line = bufferedReader.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public FrequencyWordData getFrequencyWordData(String lemma) {
    return this.records.getOrDefault(lemma, new FrequencyWordData("s", 0.0));
  }
}
