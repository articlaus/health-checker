package com.walmart.econgo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.econgo.model.SlackModel;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by: Ganbat Bayar
 * On: 5/19/2019
 * Project: ServiceStatusChecker
 */
public class SlackUtil {
    private static String webhook = "..";

    public static void sendMessage(SlackModel model) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(webhook);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(model);

            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            client.execute(httpPost);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
