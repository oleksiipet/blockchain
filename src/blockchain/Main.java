package blockchain;

import blockchain.crypto.Sign;
import blockchain.crypto.SignValidator;
import blockchain.data.Message;
import blockchain.data.format.PlainMessageFormat;
import blockchain.io.FilePersister;
import blockchain.io.Persister;
import blockchain.miners.Miner;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

  private static final String OUTPUT_FILE_NAME = "blockchain.ser";
  private static final int NUMBER_OF_MINERS = 10;
  private static AtomicLong ids = new AtomicLong(1);

  public static void main(String[] args) throws Exception {

    Sign sign = new Sign("keys/privateKey");
    SignValidator signValidator = new SignValidator("keys/publicKey");

    Persister<Message> persister = new FilePersister<>(OUTPUT_FILE_NAME);
    Blockchain<Message> blockchain = new Blockchain<>(persister,
        new PlainMessageFormat(), signValidator);

    List<Thread> miners = Stream
        .generate(() -> new Thread(new Miner(blockchain, ids.getAndIncrement())))
        .limit(NUMBER_OF_MINERS)
        .collect(Collectors.toList());

    miners.forEach(Thread::start);

    Thread.sleep(1000);
    blockchain.appendData(
        new Message(blockchain.nextId(), "Tom", "Hey, I'm first!", sign.sign("Hey, I'm first!"),
            null));
    blockchain.appendData(
        new Message(blockchain.nextId(), "Sarah", "It's not fair!", sign.sign("It's not fair!"),
            null));
    Thread.sleep(2000);
    blockchain.appendData(
        new Message(blockchain.nextId(), "Sarah",
            "You always will be first because it is your blockchain!",
            sign.sign("You always will be first because it is your blockchain!"), null));
    Thread.sleep(1000);
    blockchain.appendData(
        new Message(blockchain.nextId(), "Sarah", "Anyway, thank you for this amazing chat.",
            sign.sign("Anyway, thank you for this amazing chat."), null));
    Thread.sleep(2000);
    blockchain.appendData(new Message(blockchain.nextId(), "Tom", "You're welcome :)",
        sign.sign("You're welcome :)"), null));
    blockchain
        .appendData(new Message(blockchain.nextId(), "Nick", "Hey Tom, nice chat",
            sign.sign("Hey Tom, nice chat"), null));

    miners.forEach(Thread::interrupt);
  }
}