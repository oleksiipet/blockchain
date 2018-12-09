package blockchain;

import java.io.Serializable;

public class Block implements Serializable {

  private final Integer id;
  private final Long minerId;
  private final String hashPreviousBlock;
  private final String hash;
  private final String data;
  private final Long timestamp;
  private final Integer magicNumber;
  private final Long generationTime;


  public Block(Integer id, Long minerId, String hashPreviousBlock, String hash,
      String data, Long timestamp, Integer magicNumber, Long generationTime) {
    this.id = id;
    this.minerId = minerId;
    this.hashPreviousBlock = hashPreviousBlock;
    this.hash = hash;
    this.data = data;
    this.timestamp = timestamp;
    this.magicNumber = magicNumber;
    this.generationTime = generationTime;
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

  public Integer getMagicNumber() {
    return magicNumber;
  }

  public Long getGenerationTime() {
    return generationTime;
  }

  public Long getMinerId() {
    return minerId;
  }
}
