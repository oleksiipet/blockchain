package blockchain;

import blockchain.miners.Miner;

public class Main {

  public static void main(String[] args) throws InterruptedException {

    int NUMBER_OF_MINERS = 10;

    Persister persister = new FilePersister("blockchain.ser");
    BlockGenerator blockGenerator = new BlockGenerator();
    Blockchain blockchain = new Blockchain(persister, blockGenerator);

    Thread[] miners = new Thread[NUMBER_OF_MINERS];
    for (int i = 0; i < NUMBER_OF_MINERS; i++) {
      miners[i] = new Thread(new Miner(blockchain, blockGenerator, Long.valueOf(i)));
    }
    for (Thread miner : miners) {
      miner.start();
    }
    Thread.sleep(10000);
    for (Thread miner : miners) {
      miner.interrupt();
    }
  }
}