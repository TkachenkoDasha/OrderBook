package com.tkachenko.processor;

import com.tkachenko.exception.IllegalInputException;
import com.tkachenko.parser.data.MarketOrderData;
import com.tkachenko.parser.OrderBookParser;
import com.tkachenko.parser.data.QueryData;
import com.tkachenko.parser.data.UpdateData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
// todo need to fix bid and ask at same price
// buy all the most cheap asks
// sell all the most expensive bids
public class MarketProcessor {

    private PriorityQueue<Integer> askPrices = new PriorityQueue<>();
    private PriorityQueue<Integer> bidPrices = new PriorityQueue<>(Comparator.reverseOrder());
    private HashMap<Integer, Integer> orderBook = new HashMap<>();
    private final OrderBookParser orderBookParser = new OrderBookParser();

    public Stream<String> process(Stream<String> inputLines) {
        List<String> result = new ArrayList<>();
        inputLines.forEach(line -> {
            if (line.startsWith("u")) {
                processUpdate(line);
            } else if (line.startsWith("q")) {
                processQuery(line, result);
            } else if (line.startsWith("o")) {
                processOrder(line);
            } else {
                throw new IllegalInputException("There are no such events: " + line);
            }
        });

        return result.stream();
    }

    public void clearOrderBook() {
        askPrices = new PriorityQueue<>();
        bidPrices = new PriorityQueue<>(Comparator.reverseOrder());
        orderBook = new HashMap<>();
    }

    private void processUpdate(String line) {
        UpdateData updateData = orderBookParser.parseUpdate(line);

        String type = updateData.getType();
        Integer price = updateData.getPrice();
        Integer size = updateData.getSize();
        // todo: fix
        if (type.equals("bid")) {
            bidPrices.add(price);
        } else if (type.equals("ask")) {
            askPrices.add(price);
        } else {
            throw new IllegalInputException("There are no such update: " + line);
        }

        orderBook.put(price, size);
    }

    private void processQuery(String line, List<String> result) {
        QueryData queryData = orderBookParser.parseQuery(line);
        if (queryData.getPrice() == null) {
            bestAskAndBestBidQuery(result, queryData.getType());
        } else {
            sizeQuery(result, queryData.getPrice());
        }
    }

    private void sizeQuery(List<String> result, Integer price) {
        Integer sizeFromOrderBook = orderBook.get(price);

        if (nonNull(sizeFromOrderBook)) {
            result.add(sizeFromOrderBook.toString());
        } else {
            result.add("0");
        }
    }

    private void bestAskAndBestBidQuery(List<String> result, String type) {
        if (type.equals("best_ask")) {
            Integer bestAsk = askPrices.peek();
            Integer size = orderBook.get(bestAsk);
            result.add(bestAsk + "," + size);
        } else if (type.equals("best_bid")) {
            Integer bestBid = bidPrices.peek();
            Integer size = orderBook.get(bestBid);
            result.add(bestBid + "," + size);
        } else {
            throw new IllegalInputException("There are no such query: " + type);
        }
    }

    private void processOrder(String line) {
        MarketOrderData marketOrderData = orderBookParser.parseMarketOrder(line);

        if (marketOrderData.getType().equals("sell")) {
            doSell(marketOrderData.getSize());
        } else if (marketOrderData.getType().equals("buy")) {
            doBuy(marketOrderData.getSize());
        } else {
            throw new IllegalInputException("There are no such market orders: " + line);
        }

    }

    private void doBuy(int size) {
        Integer minPrice = askPrices.peek();
        Integer sizeByMinPrice = orderBook.get(minPrice);
        if (size > sizeByMinPrice) {
            orderBook.remove(minPrice);
            askPrices.remove(minPrice);
            size -= sizeByMinPrice;

            orderBook.put(minPrice, size);
            bidPrices.add(minPrice);
        } else if (size == sizeByMinPrice) {
            orderBook.remove(minPrice);
            askPrices.poll();
        } else {
            int newSize = sizeByMinPrice - size;
            orderBook.put(minPrice, newSize);
        }
    }

    private void doSell(int size) {
        Integer maxPrice = bidPrices.peek();
        Integer sizeByMaxPrice = orderBook.get(maxPrice);
        if (size > sizeByMaxPrice) {
            orderBook.remove(maxPrice);
            bidPrices.remove(maxPrice);
            size -= sizeByMaxPrice;

            orderBook.put(maxPrice, size);
            askPrices.add(maxPrice);
        } else if (size == sizeByMaxPrice) {
            orderBook.remove(maxPrice);
            bidPrices.poll();
        } else {
            int newSize = sizeByMaxPrice - size;
            orderBook.put(maxPrice, newSize);
        }
    }

}
