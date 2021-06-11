package com.tkachenko.processor;

import com.tkachenko.parser.OrderBookParser;
import com.tkachenko.parser.data.MarketOrderData;
import com.tkachenko.parser.data.QueryData;
import com.tkachenko.parser.data.UpdateData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MarketProcessor {

    private final PriorityQueue<Integer> askPrices = new PriorityQueue<>();
    private final PriorityQueue<Integer> bidPrices = new PriorityQueue<>(Comparator.reverseOrder());
    private final HashMap<Integer, Integer> orderBook = new HashMap<>();
    private final OrderBookParser orderBookParser = new OrderBookParser();

    public List<String> process(Stream<String> inputLines) {
        List<String> result = new ArrayList<>();
        inputLines.forEach(line -> {
            if (line.charAt(0) == 'u') {
                processUpdate(line);
            } else if (line.charAt(0) == 'q') {
                processQuery(line, result);
            } else if (line.charAt(0) == 'o') {
                processOrder(line);
            }
        });

        return result;
    }

    private void processUpdate(String line) {
        UpdateData updateData = orderBookParser.parseUpdate(line);

        String type = updateData.getType();
        int price = updateData.getPrice();
        int size = updateData.getSize();

        if (price <= 0 || size < 0) {
            return;
        }

        if (type.equals("bid")) {
            while ((size > 0) && !askPrices.isEmpty() && price >= askPrices.peek()) {
                Integer value = orderBook.get(askPrices.peek());
                int minSize = Math.min(size, isNull(value) ? -1 : value);
                doBuy(minSize);
                size -= minSize;

                if (size == 0) {
                    price = 0;
                }
            }
            if (price != 0) {
                addBid(price, size);
            }
        } else if (type.equals("ask")) {
            while (size > 0 && !bidPrices.isEmpty() && price <= bidPrices.peek()) {
                Integer value = orderBook.get(bidPrices.peek());
                int minSize = Math.min(size, isNull(value) ? -1 : value);
                doSell(minSize);
                size -= minSize;

                if (size == 0) {
                    price = 0;
                }
            }

            if (price != 0) {
                addAsk(price, size);
            }
        }
    }

    private void addBid(Integer price, Integer size) {
        if (size > 0) {
            orderBook.put(price, size);
            if (!bidPrices.contains(price)) {
                bidPrices.add(price);
            }
        } else {
            orderBook.remove(price);
            bidPrices.remove(price);
        }
    }

    private void addAsk(Integer price, Integer size) {
        if (size > 0) {
            orderBook.put(price, size);
            if (!askPrices.contains(price)) {
                askPrices.add(price);
            }
        } else {
            orderBook.remove(price);
            askPrices.remove(price);
        }
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
            if (Objects.nonNull(bestAsk) && Objects.nonNull(size)) {
                result.add(bestAsk + "," + size);
            }
        } else if (type.equals("best_bid")) {
            Integer bestBid = bidPrices.peek();
            Integer size = orderBook.get(bestBid);
            if (Objects.nonNull(bestBid) && Objects.nonNull(size)) {
                result.add(bestBid + "," + size);
            }
        }
    }

    private void processOrder(String line) {
        MarketOrderData marketOrderData = orderBookParser.parseMarketOrder(line);
        int size = marketOrderData.getSize();
        if (size < 0) {
            return;
        }

        if (marketOrderData.getType().equals("sell")) {
            doSell(size);
        } else if (marketOrderData.getType().equals("buy")) {
            doBuy(size);
        }
    }

    private void doBuy(int size) {
        while (!askPrices.isEmpty() && size > 0) {
            int minPrice = askPrices.peek();
            int sizeByMinPrice = orderBook.get(minPrice);

            if (sizeByMinPrice > size) {
                orderBook.put(minPrice, sizeByMinPrice - size);
            } else {
                orderBook.remove(minPrice);
                askPrices.poll();
            }
            size -= sizeByMinPrice;
        }
    }

    private void doSell(int size) {
        while (!bidPrices.isEmpty() && size > 0) {
            int maxPrice = bidPrices.peek();
            int sizeByMaxPrice = orderBook.get(maxPrice);

            if (sizeByMaxPrice > size) {
                orderBook.put(maxPrice, sizeByMaxPrice - size);
            } else {
                orderBook.remove(maxPrice);
                bidPrices.poll();
            }
            size -= sizeByMaxPrice;
        }
    }

}
