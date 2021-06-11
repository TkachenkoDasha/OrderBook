package com.tkachenko.processor;

import java.util.List;
import java.util.stream.Stream;

public interface MarketProcessor {
    List<String> process(Stream<String> inputLines);
}
