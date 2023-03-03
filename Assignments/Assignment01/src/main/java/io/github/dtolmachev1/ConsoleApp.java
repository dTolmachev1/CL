package io.github.dtolmachev1;

public class ConsoleApp {
  private static final String CORPUS = "../../Resources/corpus.xml";
  private static final String RESULT = "result.txt";

  public static void main(String[] args) {
    ArticleParser parser = new ArticleParser(RESULT);
    parser.parse(CORPUS);
  }
}
