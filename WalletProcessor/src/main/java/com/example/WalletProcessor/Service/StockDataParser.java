package com.example.WalletProcessor.Service;

import com.example.WalletProcessor.Model.StockData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class StockDataParser {

    public static StockData parseJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode result = root.path("chart").path("result").get(0);
        JsonNode meta = result.path("meta");
        JsonNode indicators = result.path("indicators").path("quote").get(0);

        StockData data = new StockData();

        data.setSymbol(meta.path("symbol").asText());
        data.setCurrency(meta.path("currency").asText());
        data.setCurrentPrice(meta.path("regularMarketPrice").asDouble());
        data.setPreviousClose(meta.path("chartPreviousClose").asDouble());
        data.setWeek52High(meta.path("fiftyTwoWeekHigh").asDouble());
        data.setWeek52Low(meta.path("fiftyTwoWeekLow").asDouble());

        List<Long> timestamps = new ArrayList<>();
        for( JsonNode ts : result.path("timestamp")){
            timestamps.add(ts.asLong());
        }
        data.setTimestamps(timestamps);

        data.setOpen(parseDoubles(indicators.path("open")));
        data.setHigh(parseDoubles(indicators.path("high")));
        data.setLow(parseDoubles(indicators.path("low")));
        data.setClose(parseDoubles(indicators.path("close")));
        data.setVolume(parseLongs(indicators.path("volume")));

        return data;
    }


     private static List<Double> parseDoubles(JsonNode arraynode){
        List<Double> list = new ArrayList<>();
        for(JsonNode node : arraynode){
            list.add(node.isNull() ? 0.0 : node.asDouble());
        }
        return list;
     }
     private static List<Long> parseLongs(JsonNode arraynode){
        List<Long> list = new ArrayList<>();
        for(JsonNode node : arraynode){
            list.add(node.isNull() ? 0L : node.asLong());
        }
        return list;
     }
}
