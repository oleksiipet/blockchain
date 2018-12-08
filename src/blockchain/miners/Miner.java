package blockchain.miners;

import blockchain.Block;
import blockchain.BlockGenerator;
import blockchain.Blockchain;

public class Miner implements Runnable {

  private final Blockchain blockchain;
  private BlockGenerator blockGenerator;
  private final Long id;

  public Miner(Blockchain blockchain, BlockGenerator blockGenerator, Long id) {
    this.blockchain = blockchain;
    this.blockGenerator = blockGenerator;
    this.id = id;
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      Block prev = blockchain.tail();
      blockchain.accept(blockGenerator.generateNextBlock(prev.getId() + 1, id,
          blockchain.getPrefix(), prev.getHash(), id.toString()));
    }
  }
}
