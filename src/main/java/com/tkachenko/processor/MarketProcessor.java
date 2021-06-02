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
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MarketProcessor {

    private PriorityQueue<Integer> askPrices = new PriorityQueue<>();
    private PriorityQueue<Integer> bidPrices = new PriorityQueue<>(Comparator.reverseOrder());
    private HashMap<Integer, Integer> orderBook = new HashMap<>();
    private final OrderBookParser orderBookParser = new OrderBookParser();

    public Stream<String> process(Stream<String> inputLines) {
        List<String> result = new ArrayList<>();
        inputLines.forEach(line -> {
            line = line.trim();
            if (line.charAt(0) == 'u') {
                processUpdate(line);
            } else if (line.charAt(0) == 'q') {
                processQuery(line, result);
            } else if (line.charAt(0) == 'o') {
                processOrder(line);
            } else {
                return;
            }
//            else {
//                throw new IllegalInputException("There are no such events: " + line);
//            }
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

        if (price <= 0 || size < 0) {
            return;
        }

        if (type.equals("bid")) {
            if (nonNull(askPrices.peek()) && price >= askPrices.peek()) {
                int boughtSize = doBuy(size, price);
                if (boughtSize > 0 && boughtSize < size) {
                    addBid(price, size - boughtSize);
                }
            } else {
                addBid(price, size);
            }
        } else if (type.equals("ask")) {
            if (nonNull(bidPrices.peek()) && price <= bidPrices.peek()) {
                int soldSize = doSell(size, price);
                if (soldSize > 0 && soldSize < size) {
                    addAsk(price, size - soldSize);
                }
            } else {
                addAsk(price, size);
            }
        }
//        else {
//            throw new IllegalInputException("There are no such update: " + line);
//        }
    }

    private void addBid(Integer price, Integer size) {
        if (bidPrices.contains(price)) {
            orderBook.put(price, size);
            return;
        }
//        if(askPrices.contains(price)) {
//            Integer askSize = orderBook.get(price);
//            if(size > askSize) {
//                askPrices.remove(price);
//                size -= askSize;
//                bidPrices.add(price);
//                orderBook.put(price, size);
//            } else if (size < askSize) {
//                size -= askSize;
//                orderBook.put(price, size);
//            } else {
//                askPrices.remove(price);
//                orderBook.remove(price);
//            }
//        } else {
            bidPrices.add(price);
            orderBook.put(price, size);
        //}
    }

    private void addAsk(Integer price, Integer size) {
        if (askPrices.contains(price)) {
            orderBook.put(price, size);
            return;
        }
//        if (bidPrices.contains(price)) {
//            Integer bidSize = orderBook.get(price);
//            if (size > bidSize) {
//                bidPrices.remove(price);
//                size -= bidSize;
//                askPrices.add(price);
//                orderBook.put(price, size);
//            } else if(size < bidSize) {
//                size -= bidSize;
//                orderBook.put(price, size);
//            } else {
//                bidPrices.remove(price);
//                orderBook.remove(price);
//            }
//        } else {
            askPrices.add(price);
            orderBook.put(price, size);
//        }
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
//        else {
//            throw new IllegalInputException("There are no such query: " + type);
//        }
    }

    private void processOrder(String line) {
        MarketOrderData marketOrderData = orderBookParser.parseMarketOrder(line);

        Integer size = marketOrderData.getSize();
        if (size < 0) {
            return;
        }

        if (marketOrderData.getType().equals("sell")) {
            doSell(size, null);
        } else if (marketOrderData.getType().equals("buy")) {
            doBuy(size, null);
        }
//        else {
//            throw new IllegalInputException("There are no such market orders: " + line);
//        }

    }

    private int doBuy(int size, Integer price) {
        int sumOfBoughtSizes = 0;

        Integer minPrice = askPrices.peek();
        Integer sizeByMinPrice = orderBook.get(minPrice);

        if (isNull(minPrice) || isNull(sizeByMinPrice)) {
            return sumOfBoughtSizes;
        }

        if (size >= sizeByMinPrice) {
            while (((nonNull(sizeByMinPrice) && isNull(price))
                    || (nonNull(price) && nonNull(minPrice) && nonNull(sizeByMinPrice) && price >= minPrice))
                    && !orderBook.isEmpty() && !askPrices.isEmpty()) {
                if (size < sizeByMinPrice) {
                    int newSize = sizeByMinPrice - size;
                    orderBook.put(minPrice, newSize);
                    sumOfBoughtSizes += size;
                    break;
                }

                orderBook.remove(minPrice);
                askPrices.poll();
                sumOfBoughtSizes += sizeByMinPrice;

                size -= sizeByMinPrice;

                minPrice = askPrices.peek();
                sizeByMinPrice = orderBook.get(minPrice);
            }
        } else {
            int newSize = sizeByMinPrice - size;
            orderBook.put(minPrice, newSize);

            sumOfBoughtSizes += size;
        }

        return sumOfBoughtSizes;
    }

    private int doSell(int size, Integer price) {
        int sumOfSoldSizes = 0;

        Integer maxPrice = bidPrices.peek();
        Integer sizeByMaxPrice = orderBook.get(maxPrice);

        if (isNull(maxPrice) || isNull(sizeByMaxPrice)) {
            return sumOfSoldSizes;
        }

        if (size >= sizeByMaxPrice) {
            while (((nonNull(sizeByMaxPrice) && isNull(price))
                    || (nonNull(price) && nonNull(maxPrice) && nonNull(sizeByMaxPrice) && price <= maxPrice))
                    && !orderBook.isEmpty() && !bidPrices.isEmpty()) {
                if (size < sizeByMaxPrice) {
                    int newSize = sizeByMaxPrice - size;
                    orderBook.put(maxPrice, newSize);
                    sumOfSoldSizes += size;
                    break;
                }

                orderBook.remove(maxPrice);
                bidPrices.poll();
                sumOfSoldSizes += sizeByMaxPrice;

                size -= sizeByMaxPrice;

                maxPrice = bidPrices.peek();
                sizeByMaxPrice = orderBook.get(maxPrice);
            }
        } else {
            int newSize = sizeByMaxPrice - size;
            orderBook.put(maxPrice, newSize);

            sumOfSoldSizes += size;
        }

        return sumOfSoldSizes;
    }

}
