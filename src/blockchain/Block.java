package blockchain;

public class Block {

  private final Integer id;
  private final String hashPreviousBlock;
  private final String hash;
  private final String data;
  private final Long timestamp;


  public Block(Integer id, String hashPreviousBlock, String hash, String data,
      Long timestamp) {
    this.id = id;
    this.hashPreviousBlock = hashPreviousBlock;
    this.hash = hash;
    this.data = data;
    this.timestamp = timestamp;
  }

  public Integer getId() {
    return id;
  }

  public String getHashPreviousBlock() {
    return hashPreviousBlock;
  }

  public String getHash() {
    return hash;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getData() {
    return data;
  }
}
