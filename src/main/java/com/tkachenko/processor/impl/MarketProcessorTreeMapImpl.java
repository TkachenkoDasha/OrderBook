package com.tkachenko.processor.impl;

import com.tkachenko.processor.MarketProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MarketProcessorTreeMapImpl implements MarketProcessor {

    private final TreeMap<Integer, Integer> asks = new TreeMap<>();
    private final TreeMap<Integer, Integer> bids = new TreeMap<>(Comparator.reverseOrder());

    @Override
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
        String[] parsed = line.split(",");
        int price = Integer.parseInt(parsed[1]);
        int size = Integer.parseInt(parsed[2]);
        String type = parsed[3];

        if (price <= 0 || size < 0) {
            return;
        }

        if (type.equals("bid")) {
            while ((size > 0) && !asks.isEmpty() && price >= asks.firstKey()) {
                Integer value = asks.get(asks.firstKey());
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
            while (size > 0 && !bids.isEmpty() && price <= bids.firstKey()) {
                Integer value = bids.get(bids.firstKey());
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
            bids.put(price, size);
        } else {
            bids.remove(price);
        }
    }

    private void addAsk(Integer price, Integer size) {
        if (size > 0) {
            asks.put(price, size);
        } else {
            asks.remove(price);
        }
    }

    private void processQuery(String line, List<String> result) {
        String[] parsed = line.split(",");
        Integer price = null;
        if (parsed.length == 3) {
            price = Integer.parseInt(parsed[2]);
        }

        String type = parsed[1];

        if (price == null) {
            bestAskAndBestBidQuery(result, type);
        } else {
            sizeQuery(result, price);
        }
    }

    private void sizeQuery(List<String> result, Integer price) {
        Integer sizeFromAsks = asks.get(price);
        Integer sizeFromBids = bids.get(price);

        if (nonNull(sizeFromAsks)) {
            result.add(sizeFromAsks.toString());
        } else if (nonNull(sizeFromBids)) {
            result.add(sizeFromBids.toString());
        } else {
            result.add("0");
        }
    }

    private void bestAskAndBestBidQuery(List<String> result, String type) {
        if (type.equals("best_ask")) {
            Integer bestAsk = asks.firstKey();
            Integer size = asks.get(bestAsk);
            if (Objects.nonNull(bestAsk) && Objects.nonNull(size)) {
                result.add(bestAsk + "," + size);
            }
        } else if (type.equals("best_bid")) {
            Integer bestBid = bids.firstKey();
            Integer size = bids.get(bestBid);
            if (Objects.nonNull(bestBid) && Objects.nonNull(size)) {
                result.add(bestBid + "," + size);
            }
        }
    }

    private void processOrder(String line) {
        String[] parsed = line.split(",");
        String type = parsed[1];
        int size = Integer.parseInt(parsed[2]);

        if (size < 0) {
            return;
        }

        if (type.equals("sell")) {
            doSell(size);
        } else if (type.equals("buy")) {
            doBuy(size);
        }
    }

    private void doBuy(int size) {
        while (!asks.isEmpty() && size > 0) {
            int minPrice = asks.firstKey();
            int sizeByMinPrice = asks.get(minPrice);

            if (sizeByMinPrice > size) {
                asks.put(minPrice, sizeByMinPrice - size);
            } else {
                asks.remove(minPrice);
            }
            size -= sizeByMinPrice;
        }
    }

    private void doSell(int size) {
        while (!bids.isEmpty() && size > 0) {
            int maxPrice = bids.firstKey();
            int sizeByMaxPrice = bids.get(maxPrice);

            if (sizeByMaxPrice > size) {
                bids.put(maxPrice, sizeByMaxPrice - size);
            } else {
                bids.remove(maxPrice);
            }
            size -= sizeByMaxPrice;
        }
    }

}
