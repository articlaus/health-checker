package com.walmart.econgo.util.helper;

import com.walmart.econgo.model.AttachmentModel;
import com.walmart.econgo.model.HeaderModel;
import com.walmart.econgo.model.ServiceModel;
import com.walmart.econgo.model.SlackModel;
import com.walmart.econgo.util.SlackUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.walmart.econgo.util.SlackUtil.sendMessage;

/**
 * Created by: Ganbat Bayar
 * On: 5/20/2019
 * Project: ServiceStatusChecker
 */
@Log4j2
public class RequestHelper {
    public static void handleError(ServiceModel model, String host, HttpResponse response) {
        log.error("Service - " + model.getName() + " is unhealthy on host - " + host + " sending Notification on Slack");
        SlackModel slackModel = new SlackModel();
        slackModel.setUsername("Health Bot");
        AttachmentModel attachmentModel = new AttachmentModel();
        attachmentModel.setColor("danger");
        attachmentModel.setTitle(model.getName() + " is unhealthy on");
        String body = "";
        if (1 == response.getStatusLine().getStatusCode())
            body = "On host -" + host + "\n is down and Refused Connection";
        else if (2 == response.getStatusLine().getStatusCode())
            body = "On Host -" + host + "\n connected but timed out after 5 seconds";
        else
            try {
                body = "Failed on Host-" + host + "\n with status code of -" + response.getStatusLine().getStatusCode() +
                        "\n Expecting status code of -" + model.getStatus() + " \n With body - \n" + new BasicResponseHandler().handleEntity(response.getEntity());
            } catch (IOException e) {
                body = " \n Failed on Host-" + host + "\n with invalid body";
                log.error(e);
            }
        attachmentModel.setText(body);
        slackModel.setAttachments(Arrays.asList(attachmentModel));
        slackModel.setText("<!here> Time = " + Instant.now() + " :broken_heart::bangbang:");
        slackModel.setIcon_emoji(":interrobang:");
        slackModel.setColor("danger");
        sendMessage(slackModel);
    }

    public static void sendReport(ServiceModel model, Integer count, Integer total) {
        SlackModel message = new SlackModel();
        String stat;
        String color;
        if (count.equals(total)) {
            stat = "OK";
            color = "good";
        } else if (count.equals(0)) {
            stat = "DOWN";
            color = "danger";
        } else {
            stat = "Partially Healthy";
            color = "warning";
        }
        message.setText(model.getName() + " is " + stat);
        AttachmentModel attach = new AttachmentModel();
        attach.setText(count + " / " + total + "\n" + Instant.now());
        attach.setTitle("Is healthy on");
        attach.setColor(color);
        message.setAttachments(Arrays.asList(attach));
        message.setChannel("educational");
        message.setUsername("Health Bot");
        message.setIcon_emoji(":heart:");
        message.setColor("good");
        sendMessage(message);
    }

    public static void setHeaders(HttpRequestBase request, List<HeaderModel> headers) {
        for (HeaderModel header : headers) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }
}
