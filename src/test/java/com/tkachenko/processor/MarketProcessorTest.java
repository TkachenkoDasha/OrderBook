package com.tkachenko.processor;

import com.tkachenko.processor.impl.MarketProcessorImpl;
import com.tkachenko.processor.impl.MarketProcessorTreeMapImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketProcessorTest {

    MarketProcessor marketProcessor = new MarketProcessorTreeMapImpl();


    @Test
    @DisplayName("getSizeByPrice()")
    void getSizeByPrice() {
        List<String> collect = marketProcessor.process(
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
        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
        assertEquals("0", collect.get(3));
        assertEquals("0", collect.get(4));
        assertEquals("0", collect.get(5));
        assertEquals("0", collect.get(6));
    }

    @Test
    @DisplayName("peek()")
    void getPeek() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,20,5,ask",
                        "u,21,10,ask",
                        "u,18,15,ask",
                        "q,best_ask")
        );

        assertEquals("18,15", collect.get(0));

        List<String> collect1 = marketProcessor.process(
                Stream.of(
                        "u,9,10,bid",
                        "u,8,5,bid",
                        "u,10,15,bid",
                        "q,best_bid")
        );
        assertEquals("10,15", collect1.get(0));
    }


    @Test
    void removeOrders() {
        List<String> collect = marketProcessor.process(
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

        assertEquals("2", collect.get(0));
        assertEquals("5", collect.get(1));
        assertEquals("2", collect.get(2));
        assertEquals("5", collect.get(3));
    }

    @Test
    void removeOrders1() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "o,sell,10",
                        "q,size,9")
        );

        assertEquals("5", collect.get(0));
    }

    @Test
    void removeOrders2() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,10,5,bid",
                        "u,9,5,bid",
                        "u,8,5,bid",
                        "o,sell,20",
                        "q,size,8",
                        "q,size,9",
                        "q,size,10")
        );

        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
    }

    @Test
    void removeOrders3() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,10,5,ask",
                        "u,9,10,ask",
                        "u,8,15,ask",
                        "o,buy,20",
                        "q,size,9")
        );

        assertEquals("5", collect.get(0));
    }

    @Test
    void removeOrders4() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,10,5,ask",
                        "u,9,5,ask",
                        "u,8,5,ask",
                        "o,buy,35",
                        "q,size,8",
                        "q,size,9",
                        "q,size,10")
        );

        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
    }

    @Test
    @DisplayName("insertMiddleOrder()")
    void insertMiddleOrder() {
        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,22,15,ask",
                        "u,21,10,ask",
                        "u,20,5,ask",
                        "u,21,20,bid",
                        "q,size,21",
                        "u,10,5,bid",
                        "u,9,10,bid",
                        "u,8,15,bid",
                        "u,9,30,ask",
                        "q,size,9")
        );

        assertEquals("5", collect.get(0));
        assertEquals("10", collect.get(1));
    }

    @Test
    @DisplayName("insertMiddleOrder()")
    void insertMiddleOrder2() {
        List<String> collect = marketProcessor.process(
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

        assertEquals("5", collect.get(0));
        assertEquals("5", collect.get(1));
    }

    @Test
    void process() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
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


        assertEquals("9,1", collect.get(0));
        assertEquals("10,2", collect.get(1));
        assertEquals("1", collect.get(2));
    }

    @Test
    void process5() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
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


        assertEquals("5", collect.get(0));
        assertEquals("5", collect.get(1));
    }

    @Test
    void process6() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
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


        assertEquals("0", collect.get(0));
        assertEquals("0", collect.get(1));
        assertEquals("0", collect.get(2));
        assertEquals("0", collect.get(3));
        assertEquals("0", collect.get(4));
        assertEquals("0", collect.get(5));
    }

    @Test
    void process1() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,9,1,bid",
                        "u,9,5,ask",
                        "q,best_bid",
                        "q,best_ask"
                )
        );


        assertEquals("9,4", collect.get(0));
    }

    @Test
    void process2() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of("q,best_bid")
        );


        assertTrue(collect.isEmpty());
    }

    @Test
    void process3() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of("q,best_ask")
        );


        assertTrue(collect.isEmpty());
    }

    @Test
    void buyNothing() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of("o,buy,20")
        );


        assertTrue(collect.isEmpty());
    }

    @Test
    void process4() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of("q,size,19")
        );


        assertEquals("0", collect.get(0));
    }

    @Test
    void process7() {
        MarketProcessorImpl marketProcessor = new MarketProcessorImpl();

        List<String> collect = marketProcessor.process(
                Stream.of(
                        "u,9,1,bid",
                        "u,9,5,ask",
                        "q,best_bid",
                        "q,best_ask"
                )
        );


        assertEquals("9,4", collect.get(0));
    }
}