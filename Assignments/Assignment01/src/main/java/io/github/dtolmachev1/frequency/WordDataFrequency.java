package io.github.dtolmachev1.frequency;

public class WordDataFrequency {
  private double entryCount;
  private int lastTextId;
  private double textCount;

  public WordDataFrequency() {
    this.entryCount = 0.0;
    this.lastTextId = -1;
    this.textCount = 0.0;
  }

  public WordDataFrequency(double entryCount, int lastTextId, double textCount) {
    this.entryCount = entryCount;
    this.lastTextId = lastTextId;
    this.textCount = textCount;
  }

  public double getEntryCount() {
    return this.entryCount;
  }

  public void setEntryCount(double entryCount) {
    this.entryCount = entryCount;
  }

  public int getLastTextId() {
    return this.lastTextId;
  }

  public void setLastTextId(int lastTextId) {
    this.lastTextId = lastTextId;
  }

  public double getTextCount() {
    return this.textCount;
  }

  public void setTextCount(double textCount) {
    this.textCount = textCount;
  }

  public void increaseEntryCount() {
    this.entryCount++;
  }

  public void increaseTextCount() {
    this.textCount++;
  }
}
