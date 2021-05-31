package com.tkachenko.processor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketProcessorTest {

    MarketProcessor marketProcessor = new MarketProcessor();


    @Test
    @DisplayName("getSizeByPrice()")
    void getSizeByPrice() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,20,5,bid",
                        "u,21,10,bid",
                        "u,22,15,bid",
                        "u,10,5,ask",
                        "u,9,10,ask",
                        "u,8,15,ask",
                        "q,size,20",
                        "q,size,21",
                        "q,size,22",
                        "q,size,10",
                        "q,size,9",
                        "q,size,8",
                        "q,size,888")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("5", collect.get(0));
        assertEquals("10", collect.get(1));
        assertEquals("15", collect.get(2));
        assertEquals("5", collect.get(3));
        assertEquals("10", collect.get(4));
        assertEquals("15", collect.get(5));
        assertEquals("0", collect.get(6));
    }

    @Test
    @DisplayName("peek()")
    void getPeek() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,20,5,ask",
                        "u,21,10,ask",
                        "u,18,15,ask",
                        "q,best_ask")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("18,15", collect.get(0));

        Stream<String> process1 = marketProcessor.process(
                Stream.of(
                        "u,9,10,bid",
                        "u,8,5,bid",
                        "u,10,15,bid",
                        "q,best_bid")
        );
        List<String> collect1 = process1.collect(Collectors.toList());
        assertEquals("10,15", collect1.get(0));
    }


    @Test
    void removeOrders() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,22,15,ask",
                        "u,21,10,ask",
                        "u,20,5,ask",
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "o,sell,3",
                        "q,size,10",
                        "o,sell,7",
                        "q,size,9",
                        "o,buy,3",
                        "q,size,20",
                        "o,buy,7",
                        "q,size,21")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("2", collect.get(0));
        assertEquals("5", collect.get(1));
        assertEquals("2", collect.get(2));
        assertEquals("5", collect.get(3));
    }

    @Test
    void removeOrders1() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "o,sell,10",
                        "q,size,9")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("5", collect.get(0));
    }

    @Test
    void removeOrders2() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,10,5,bid",
                        "u,9,5,bid",
                        "u,8,5,bid",
                        "o,sell,20",
                        "q,size,8",
                        "q,size,9",
                        "q,size,10")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
    }

    @Test
    void removeOrders3() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,10,5,ask",
                        "u,9,10,ask",
                        "u,8,15,ask",
                        "o,buy,20",
                        "q,size,9")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("5", collect.get(0));
    }

    @Test
    void removeOrders4() {
        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,10,5,ask",
                        "u,9,5,ask",
                        "u,8,5,ask",
                        "o,buy,35",
                        "q,size,8",
                        "q,size,9",
                        "q,size,10")
        );
        List<String> collect = process.collect(Collectors.toList());
        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
    }

//    @Test
//    @DisplayName("insertMiddleOrder()")
//    void insertMiddleOrder() {
//        Stream<String> process = marketProcessor.process(
//                Stream.of(
//                        "u,22,15,ask",
//                        "u,21,10,ask",
//                        "u,20,5,ask",
//                        "u,21,20,bid",
//                        "q,size,21",
//                        "u,10,5,bid",
//                        "u,9,10,bid",
//                        "u,8,15,bid",
//                        "u,9,30,ask",
//                        "q,size,9")
//        );
//        List<String> collect = process.collect(Collectors.toList());
//        assertEquals("21,5", collect.get(0));
//        assertEquals("9,10", collect.get(1));
//    }
//
//    @Test
//    @DisplayName("insertMiddleOrder()")
//    void insertMiddleOrder2() {
//        shares.parseString("u,22,15,ask");
//        shares.parseString("u,21,10,ask");
//        shares.parseString("u,20,5,ask");
//
//        shares.parseString("u,23,10,bid");
//        assertEquals(21, askOrders.peekKey());
//        assertEquals("5", shares.getSizeByPrice(askOrders.peekKey()));
//
//        shares.parseString("u,10,5,bid");
//        shares.parseString("u,9,10,bid");
//        shares.parseString("u,8,15,bid");
//
    //        shares.parseString("u,5,10,ask");
//        assertEquals(9, bidOrders.peekKey());
//        assertEquals("5", shares.getSizeByPrice(bidOrders.peekKey()));
//    }

    @Test
    void process() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,9,1,bid",
                        "u,11,5,ask",
                        "q,best_bid",
                        "u,10,2,bid",
                        "q,best_bid",
                        "o,sell,1",
                        "q,size,10",
                        "u,9,0,bid",
                        "u,11,0,ask")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertEquals("9,1", collect.get(0));
        assertEquals("10,2", collect.get(1));
        assertEquals("1", collect.get(2));
    }

    @Test
    void process5() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,22,15,ask",
                        "u,21,10,ask",
                        "u,20,5,ask",
                        "u,23,10,bid",
                        "q,size,21",
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "u,5,10,ask",
                        "q,size,9")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertEquals("5", collect.get(0));
        assertEquals("5", collect.get(1));
    }

    @Test
    void process6() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,20,5,ask",
                        "u,21,10,ask",
                        "u,22,15,ask",
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "o,sell,100",
                        "o,buy,100",
                        "q,size,20",
                        "q,size,21",
                        "q,size,22",
                        "q,size,10",
                        "q,size,9",
                        "q,size,8")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
        assertEquals("0", collect.get(3));
        assertEquals("0", collect.get(4));
        assertEquals("0", collect.get(5));
    }

    @Test
    void process1() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of(
                        "u,9,1,bid",
                        "u,9,5,ask",
                        "q,best_bid",
                        "q,best_ask"
                )
        );

        List<String> collect = process.collect(Collectors.toList());
        assertEquals("9,4", collect.get(0));
    }

    @Test
    void process2() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of("q,best_bid")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertTrue(collect.isEmpty());
    }

    @Test
    void process3() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of("q,best_ask")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertTrue(collect.isEmpty());
    }

    @Test
    void process4() {
        MarketProcessor marketProcessor = new MarketProcessor();

        Stream<String> process = marketProcessor.process(
                Stream.of("q,size,19")
        );

        List<String> collect = process.collect(Collectors.toList());
        assertEquals("0", collect.get(0));
    }

    @Test
    void clearOrderBook() {
    }
}