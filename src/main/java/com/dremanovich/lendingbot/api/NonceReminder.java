package com.dremanovich.lendingbot.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class NonceReminder implements AutoCloseable {

    private static Logger log = LogManager.getLogger(NonceReminder.class);

    private AtomicLong nonce = new AtomicLong();

    private Path nonceFile;

    public NonceReminder(Path nonceFile) {
        this.nonceFile = nonceFile;

        try (Stream<String> stream = Files.lines(nonceFile)) {

            Optional<String> line = stream.findFirst();

            if (line.isPresent()){
                Long longValue = Long.parseLong(line.get());
                nonce.set(longValue);
            }

        } catch (NumberFormatException | NullPointerException | IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    public long get(){
        return nonce.get();
    }

    public void next(){
        nonce.incrementAndGet();
    }

    @Override
    public synchronized void close() throws Exception {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(nonceFile), "utf-8"))) {
            writer.write(nonce.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
