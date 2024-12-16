package org.tinc.core;

import java.io.*;

// Encoder interface
interface Encoder<T> {
    void encode(T obj) throws IOException;
}

// Decoder interface
interface Decoder<T> {
    T decode() throws IOException, ClassNotFoundException;
}

// Transaction Encoder using ObjectOutputStream
class GobTxEncoder implements Encoder<Transaction> {
    private final OutputStream outputStream;

    public GobTxEncoder(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void encode(Transaction transaction) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(transaction);
        }
    }
}

// Transaction Decoder using ObjectInputStream
class GobTxDecoder implements Decoder<Transaction> {
    private final InputStream inputStream;

    public GobTxDecoder(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Transaction decode() throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (Transaction) objectInputStream.readObject();
        }
    }
}

// Block Encoder using ObjectOutputStream
class GobBlockEncoder implements Encoder<Block> {
    private final OutputStream outputStream;

    public GobBlockEncoder(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void encode(Block block) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(block);
        }
    }
}

// Block Decoder using ObjectInputStream
class GobBlockDecoder implements Decoder<Block> {
    private final InputStream inputStream;

    public GobBlockDecoder(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Block decode() throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (Block) objectInputStream.readObject();
        }
    }
}
