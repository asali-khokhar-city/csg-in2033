package ac.csg.pu.comms.client;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ac.csg.pu.comms.model.MutateStockRequest;
import ac.csg.pu.comms.model.StockItem;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CAClient {

    private static final Logger logger = LoggerFactory.getLogger(CAClient.class);

    private static final OkHttpClient client = new OkHttpClient();

    private static final String BASE_URL = "http://localhost:8088";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static ArrayList<StockItem> getCatalogue() {

        Request request = new Request.Builder()
                .url(BASE_URL + "/stock/all")
                .build();

        Call call = client.newCall(request);
        try {

            Response response = call.execute();

            if (response.code() == 200) {

                ObjectMapper mapper = new ObjectMapper();
                ArrayList<StockItem> stocks = mapper.readValue(response.body().string(),
                        new TypeReference<ArrayList<StockItem>>() {
                        });
                return stocks;

            } else {

                logger.error("CA client query retuned unexpected code: {}", response.code());
                return new ArrayList<StockItem>();
            }
        } catch (IOException e) {
            logger.error("CA client query failed: {}", e);
            return new ArrayList<StockItem>();
        }

    }

    public static boolean mutateStock(StockItem order) {
        // Create a MutateStockRequest based on the item's id and *current* quantity

        ObjectMapper mapper = new ObjectMapper();

        MutateStockRequest msr = new MutateStockRequest(order.id, order.quantity);

        String jsonStr;
        try {
            jsonStr = mapper.writeValueAsString(msr);
        } catch (JsonProcessingException e) {
            logger.error("CA mutate stock request failed: {}", e);
            return false;
        }

        RequestBody body = RequestBody.create(jsonStr, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/stock/mutate")
                .post(body)
                .build();

        Call call = client.newCall(request);

        try {

            Response response = call.execute();

            if (response.code() == 200) {
                return true;
            } else {
                logger.warn("Unexpected HTTP response code '{}' when mutating stock", response.code());
            }
        } catch (IOException e) {

            logger.error("CA mutate stock request failed: {}", e);
        }

        return false;
    }
}