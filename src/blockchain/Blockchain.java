package blockchain;

import blockchain.data.format.DataFormatter;
import blockchain.io.Persister;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Blockchain<T> {

  private static final Long ACCEPTABLE_TIME = TimeUnit.SECONDS.toMillis(5);

  private final List<Block> blocks;
  private final List<T> pendingMessages;
  private int prefixLength;
  private final Persister persister;
  private DataFormatter<T> dataFormatter;

  public Blockchain(Persister persister, DataFormatter<T> dataFormatter) {
    this.persister = persister;
    this.dataFormatter = dataFormatter;
    this.prefixLength = 0;
    List<Block> blocks = persister.load();
    if (!blocks.isEmpty() && validateAllChain(blocks)) {
      this.blocks = blocks;
    } else {
      this.blocks = new LinkedList<>(Collections.singleton(
          new Block(1, 0L, "", "0", new Date().getTime(), 0)));
      persister.save(blocks);
    }
    pendingMessages = new LinkedList<>();
  }

  public synchronized void accept(Block newBlock) {
    if (isValid(newBlock)) {
      newBlock.setData(pendingData());
      outputAndAdjust(newBlock);
      blocks.add(newBlock);
      pendingMessages.clear();
      persister.save(blocks);
    }
  }

  public synchronized void appendData(T data) {
    pendingMessages.add(data);
  }

  public synchronized String getPrefix() {
    return Stream.iterate("0", x -> "0")
        .limit(prefixLength)
        .reduce("", (x, y) -> x + y);
  }

  public synchronized String pendingData() {
    StringJoiner stringJoiner = new StringJoiner("\n");
    if (pendingMessages.isEmpty()) {
      return "no messages";
    }
    pendingMessages.stream()
        .map(dataFormatter::format)
        .forEach(stringJoiner::add);
    return stringJoiner.toString();
  }

  public synchronized Block tail() {
    return blocks.get(blocks.size() - 1);
  }

  private boolean isValid(Block newBlock) {
    Block tailBlock = blocks.get(blocks.size() - 1);
    if (!newBlock.getHash().startsWith(getPrefix()) || !newBlock.getHashPreviousBlock()
        .equals(tailBlock.getHash())) {
      return false;
    }
    return true;
  }

  private void outputAndAdjust(Block newBlock) {
    outputStats(newBlock);
    adjustComplexity(newBlock);
  }

  private void outputStats(Block newBlock) {
    System.out.printf("Block:\n");
    System.out.printf("Id: %s\n", newBlock.getId());
    System.out.printf("Created by miner # %s\n", newBlock.getMinerId());
    System.out.printf("Timestamp: %s\n", newBlock.getTimestamp());
    System.out.printf("Magic number: %s\n", newBlock.getMagicNumber());
    System.out.printf("Hash of the previous block:\n%s\n", newBlock.getHashPreviousBlock());
    System.out.printf("Block data: \n%s\n", newBlock.getData());
    System.out.printf("Magic number: %s\n", newBlock.getMagicNumber());
    System.out
        .printf("Block was generating for: %s seconds\n", getGenerationTime(newBlock) / 1000);
  }

  private void adjustComplexity(Block newBlock) {
    if (!withinAcceptable(newBlock)) {
      if (ACCEPTABLE_TIME < getGenerationTime(newBlock)) {
        if (prefixLength > 0) {
          prefixLength--;
        }
        System.out.printf("N was decreased to %s\n\n", prefixLength);
      } else {
        prefixLength++;
        System.out.printf("N was increased to %s\n\n", prefixLength);
      }
    } else {
      System.out.printf("N stays the same\n\n");
    }
  }

  private Long getGenerationTime(Block newBlock) {
    return tail() == null ? 0 : (newBlock.getTimestamp() - tail().getTimestamp());
  }

  private boolean withinAcceptable(Block newBlock) {
    return Math.abs(getGenerationTime(newBlock) - ACCEPTABLE_TIME) <= 1000;
  }

  private boolean validateAllChain(List<Block> blocks) {
    for (int i = 1; i < blocks.size(); i++) {
      Block prev = blocks.get(i - 1);
      Block cur = blocks.get(i);
      if (!cur.getHashPreviousBlock().equals(prev.getHash())) {
        return false;
      }
    }
    return true;
  }

}
