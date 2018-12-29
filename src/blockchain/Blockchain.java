package blockchain;

import blockchain.crypto.SignValidator;
import blockchain.data.SignedData;
import blockchain.data.format.DataFormatter;
import blockchain.io.Persister;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blockchain<T extends SignedData & Serializable> {

  private static final Long ACCEPTABLE_TIME = TimeUnit.SECONDS.toMillis(5);

  private final ReentrantReadWriteLock readWritelock;

  private final List<Block<T>> blocks;
  private final List<T> pendingMessages;
  private final Persister persister;

  private int prefixLength;
  private DataFormatter<T> dataFormatter;
  private SignValidator signValidator;

  private AtomicInteger idGenerator;

  public Blockchain(Persister<T> persister, DataFormatter<T> dataFormatter,
      SignValidator signValidator) {
    this.readWritelock = new ReentrantReadWriteLock();
    this.persister = persister;
    this.dataFormatter = dataFormatter;
    this.signValidator = signValidator;
    this.prefixLength = 0;
    List<Block<T>> fileBlocks = persister.load();
    if (!fileBlocks.isEmpty() && validateAllChain(fileBlocks)) {
      this.blocks = fileBlocks;
    } else {
      this.blocks = new LinkedList<>(Collections.singleton(
          new BlockBuilder<T>()
              .withId(1)
              .withMinerId(0L)
              .withHashPreviousBlock("")
              .withHash("0")
              .withMagicNumber(0)
              .withTimestamp(new Date().getTime()).build()));
      persister.save(fileBlocks);
    }
    pendingMessages = new LinkedList<>();
    this.idGenerator = new AtomicInteger(blocks.get(blocks.size() - 1).getId());
  }

  public void accept(Block newBlock) {
    WriteLock writeLock = readWritelock.writeLock();
    try {
      writeLock.lock();
      if (isValid(newBlock)) {
        newBlock.setData(new ArrayList<>(pendingMessages));
        outputAndAdjust(newBlock);
        blocks.add(newBlock);
        pendingMessages.clear();
        persister.save(blocks);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void appendData(T data) {
    WriteLock writeLock = readWritelock.writeLock();
    try {
      writeLock.lock();
      if (isSigned(dataFormatter.format(data), data.dataSignature())) {
        pendingMessages.add(data);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public String getPrefix() {
    ReadLock readLock = readWritelock.readLock();
    try {
      readLock.lock();
      return Stream.iterate("0", x -> "0")
          .limit(prefixLength)
          .reduce("", (x, y) -> x + y);
    } finally {
      readLock.unlock();
    }
  }

  public String pendingData() {
    ReadLock readLock = readWritelock.readLock();
    try {
      readLock.lock();
      StringJoiner stringJoiner = new StringJoiner("\n");
      if (pendingMessages.isEmpty()) {
        return "no messages";
      }
      pendingMessages.stream()
          .map(dataFormatter::format)
          .forEach(stringJoiner::add);
      return stringJoiner.toString();
    } finally {
      readLock.unlock();
    }
  }

  public Block tail() {
    ReadLock readLock = readWritelock.readLock();
    try {
      readLock.lock();
      return blocks.get(blocks.size() - 1);
    } finally {
      readLock.unlock();
    }
  }

  public Integer nextId() {
    return idGenerator.incrementAndGet();
  }

  private boolean isSigned(String data, byte[] dataSignature) {
    try {
      return true;//return signValidator.verifySignature(data.getBytes(), dataSignature);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isValid(Block newBlock) {
    Block tailBlock = blocks.get(blocks.size() - 1);
    if (!newBlock.getHash().startsWith(getPrefix()) || !Objects
        .equals(newBlock.getHashPreviousBlock(), tailBlock.getHash())) {
      return false;
    }
    return true;
  }

  private void outputAndAdjust(Block newBlock) {
    outputStats(newBlock);
    adjustComplexity(newBlock);
  }

  private void outputStats(Block<T> newBlock) {
    System.out.printf("Block:\n");
    System.out.printf("Id: %s\n", newBlock.getId());
    System.out.printf("Created by miner # %s\n", newBlock.getMinerId());
    System.out.printf("Timestamp: %s\n", newBlock.getTimestamp());
    System.out.printf("Magic number: %s\n", newBlock.getMagicNumber());
    System.out.printf("Hash of the previous block:\n%s\n", newBlock.getHashPreviousBlock());
    System.out.printf("Block data: \n%s\n", newBlock.getData().stream().map(
        x -> dataFormatter.format(x)
    ).collect(Collectors.joining("\n")));
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

  private Long getGenerationTime(Block<?> newBlock) {
    return tail() == null ? 0 : (newBlock.getTimestamp() - tail().getTimestamp());
  }

  private boolean withinAcceptable(Block<?> newBlock) {
    return Math.abs(getGenerationTime(newBlock) - ACCEPTABLE_TIME) <= 1000;
  }

  private boolean validateAllChain(List<Block<T>> blocks) {
    for (int i = 1; i < blocks.size(); i++) {
      Block prev = blocks.get(i - 1);
      Block cur = blocks.get(i);
      if (!Objects.equals(prev.getHash(), cur.getHashPreviousBlock())) {
        return false;
      }
    }
    return true;
  }

}
