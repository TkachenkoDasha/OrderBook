package com.tkachenko;

import com.tkachenko.processor.MarketProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        MarketProcessor marketProcessor = new MarketProcessor();
        List<String> process = marketProcessor.process(Files.lines(Path.of("input.txt")));
        //Files.write(Paths.get("output.txt"), process);
    }
}
