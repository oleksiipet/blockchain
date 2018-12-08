package blockchain;

import java.util.Random;

public class BlockGenerator {

  private final Random random;

  public BlockGenerator() {
    this.random = new Random();
  }

  public Block generateNextBlock(int id, long minerId, String prefix, String previousHash,
      String data) {
    long timestamp = System.currentTimeMillis();
    Integer magicNumber;
    String hash;
    do {
      magicNumber = random.nextInt();
      hash = StringUtil.applySha256(previousHash + magicNumber);
    } while (!hash.startsWith(prefix));
    return new Block(id, minerId, previousHash, hash, data, timestamp, magicNumber,
        System.currentTimeMillis() - timestamp);
  }
}
