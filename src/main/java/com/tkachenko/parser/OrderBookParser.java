package com.tkachenko.parser;

import com.tkachenko.exception.IllegalInputException;
import com.tkachenko.parser.data.MarketOrderData;
import com.tkachenko.parser.data.QueryData;
import com.tkachenko.parser.data.UpdateData;

public class OrderBookParser {
    public UpdateData parseUpdate(String line) {
        String[] parsed = line.split(",");
        int price = Integer.parseInt(parsed[1]);
        int size = Integer.parseInt(parsed[2]);
        String type = parsed[3];

       return new UpdateData(type, price, size);
    }

    public QueryData parseQuery(String line) {
        String[] parsed = line.split(",");
        Integer price = null;
        if (parsed.length == 3) {
            price = Integer.parseInt(parsed[2]);
        }

        String type = parsed[1];

        return new QueryData(type, price);
    }

    public MarketOrderData parseMarketOrder(String line) {
        String[] parsed = line.split(",");
        String type = parsed[1];
        int size = Integer.parseInt(parsed[2]);

        return new MarketOrderData(type, size);
    }
}
