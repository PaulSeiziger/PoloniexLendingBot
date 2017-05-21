package com.dremanovich.leadingbot.helpers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class NonceReminder {

    private Path nonceFile;

    public NonceReminder(Path nonceFile) {
        this.nonceFile = nonceFile;
    }

    public long get(){
        long nonce = 0;

        try (Stream<String> stream = Files.lines(nonceFile)) {

            Optional<String> line = stream.findFirst();

            if (line.isPresent()){
                nonce = Long.parseLong(line.get());
            }

        } catch (NumberFormatException | NullPointerException | IOException e) {
            e.printStackTrace();
        }

        return nonce;
    }

    public void save(long nonce){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(nonceFile), "utf-8"))) {
            writer.write(Long.toString(nonce));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
