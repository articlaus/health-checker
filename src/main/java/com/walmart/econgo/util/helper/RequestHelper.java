package com.walmart.econgo.util.helper;

import com.walmart.econgo.model.HeaderModel;
import com.walmart.econgo.model.SlackModel;
import com.walmart.econgo.util.SlackUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Created by: Ganbat Bayar
 * On: 5/20/2019
 * Project: ServiceStatusChecker
 */
@Log4j2
public class RequestHelper {
    public static void handleError(String name, String host, HttpResponse response) {
        log.error("Service - " + name + " is unhealthy on host - " + host + " sending Notification on Slack");
        SlackModel slackModel = new SlackModel();
        slackModel.setUsername("Health Bot");
        if (1 == response.getStatusLine().getStatusCode()) {
            slackModel.setText("Service - " + name + " \n On host -" + host + "\n is down and Refused Connectionn");
        } else if (2 == response.getStatusLine().getStatusCode()) {
            slackModel.setText("Service - " + name + " \n On Host -" + host + "\n connected but timed out after 5 seconds");
        } else {
            try {
                slackModel.setText("Service - " + name + " \n Failed on Host-" + host + "\n with status code of -" + response.getStatusLine().getStatusCode() + "\n With body - \n" + new BasicResponseHandler().handleEntity(response.getEntity()));
            } catch (IOException e) {
                slackModel.setText("Service - " + name + " \n Failed on Host-" + host + "\n with invalid body");
                log.error(e);
            }
        }
        slackModel.setText(slackModel.getText() + "\n Time = " + Instant.now());
        slackModel.setIcon_emoji(":interrobang:");
        SlackUtil.sendMessage(slackModel);
    }

    public static void setHeaders(HttpRequestBase request, List<HeaderModel> headers) {
        for (HeaderModel header : headers) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }
}
