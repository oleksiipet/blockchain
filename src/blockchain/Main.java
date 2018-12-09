package blockchain;

import blockchain.data.Message;
import blockchain.data.format.PlanMessageFormat;
import blockchain.io.FilePersister;
import blockchain.io.Persister;
import blockchain.miners.Miner;

public class Main {

  private static final String OUTPUT_FILE_NAME = "blockchain.ser";
  private static final int NUMBER_OF_MINERS = 10;

  public static void main(String[] args) throws InterruptedException {

    Persister persister = new FilePersister(OUTPUT_FILE_NAME);
    Blockchain<Message> blockchain = new Blockchain<>(persister,
        new PlanMessageFormat());

    Thread[] miners = new Thread[NUMBER_OF_MINERS];

    for (int i = 0; i < NUMBER_OF_MINERS; i++) {
      miners[i] = new Thread(new Miner(blockchain, Long.valueOf(i)));
    }
    for (Thread miner : miners) {
      miner.start();
    }
    Thread.sleep(1000);
    blockchain.appendData(new Message("Tom", "Hey, I'm first!"));
    blockchain.appendData(new Message("Sarah", "It's not fair!"));
    Thread.sleep(2000);
    blockchain.appendData(
        new Message("Sarah", "You always will be first because it is your blockchain!"));
    Thread.sleep(1000);
    blockchain.appendData(new Message("Sarah", "Anyway, thank you for this amazing chat."));
    Thread.sleep(2000);
    blockchain.appendData(new Message("Tom", "You're welcome :)"));
    blockchain.appendData(new Message("Nick", "Hey Tom, nice chat"));

    for (Thread miner : miners) {
      miner.interrupt();
    }
  }
}