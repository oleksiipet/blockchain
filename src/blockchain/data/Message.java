package blockchain.data;

public class Message {

  private String author;
  private String text;

  public Message(String author, String text) {
    this.author = author;
    this.text = text;
  }

  public String getAuthor() {
    return author;
  }

  public String getText() {
    return text;
  }
}
