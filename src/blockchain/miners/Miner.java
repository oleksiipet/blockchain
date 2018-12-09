package blockchain.miners;

import blockchain.Block;
import blockchain.Blockchain;
import blockchain.utils.StringUtil;
import java.util.Random;

public class Miner implements Runnable {

  private final Blockchain<?> blockchain;
  private final Long id;
  private final Random random;

  public Miner(Blockchain<?> blockchain, Long id) {
    this.blockchain = blockchain;
    this.id = id;
    this.random = new Random();
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      long timestamp = System.currentTimeMillis();
      Block prev;
      String prefix;
      String data;
      Integer magicNumber;
      String hash;
      do {
        prev = blockchain.tail();
        prefix = blockchain.getPrefix();
        data = blockchain.pendingData();
        magicNumber = random.nextInt();
        hash = StringUtil.applySha256(prev.getHash() + magicNumber + data);
      } while (!hash.startsWith(prefix));
      Block newBlock = new Block(prev.getId() + 1, id, prev.getHash(), hash, data, timestamp,
          magicNumber,
          System.currentTimeMillis() - timestamp);
      blockchain.accept(newBlock);
    }
  }
}
