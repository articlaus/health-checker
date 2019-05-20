package com.walmart.econgo.util;


import com.walmart.econgo.model.HeaderModel;
import com.walmart.econgo.model.ServiceModel;
import com.walmart.econgo.model.SlackModel;
import com.walmart.econgo.model.enums.MethodEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.codec.binary.Base64;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;


/**
 * Created by: Ganbat Bayar
 * On: 5/18/2019
 * Project: ServiceStatusChecker
 */
@Log4j2
public class RequestRunnable implements Runnable {

    private ServiceModel model;

    public RequestRunnable(ServiceModel model) {
        this.model = model;
    }

    private void checkServiceStatus() {
        try {
            String[] hosts = model.getHosts().split(",");
            int timeout = 5;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
            URIBuilder builder;
            for (String host : hosts) {
                builder = new URIBuilder();
                builder.setScheme("http").setHost(host).setPath(model.getUrl());

                Optional<List<HeaderModel>> params = Optional.ofNullable(model.getParams());
                if (params.isPresent())
                    for (HeaderModel param : params.get()) {
                        builder.setParameter(param.getKey(), param.getValue());
                    }
                HttpResponse response;
                try {
                    if (model.getMethod().equals(MethodEnum.GET)) {
                        HttpGet req = new HttpGet(builder.build());
                        setHeaders(req, model.getHeaders());
                        response = httpClient.execute(req);
                    } else {
                        HttpEntityEnclosingRequestBase req;
                        if (model.getMethod().equals(MethodEnum.PUT))
                            req = new HttpPut(builder.build());
                        else
                            req = new HttpPost(builder.build());
                        setHeaders(req, model.getHeaders());
                        StringEntity body = new StringEntity(new String(Base64.decodeBase64(model.getBody())));
                        req.setEntity(body);
                        response = httpClient.execute(req);
                    }

                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        handleError(model.getName(), host, response.getStatusLine().getStatusCode());
                    }
                    log.info("Service - " + model.getName() + " is healthy on host - " + host);
                } catch (HttpHostConnectException eh) {
                    handleError(model.getName(), host, -1);
                } catch (ConnectTimeoutException | SocketTimeoutException eh) {
                    handleError(model.getName(), host, -2);
                }
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void handleError(String name, String host, int statusCode) {
        log.error("Service - " + name + " is unhealthy on host - " + host + " sending Notification on Slack");
        SlackModel slackModel = new SlackModel();
        slackModel.setUsername("Health Bot");
        if (-1 == statusCode) {
            slackModel.setText("Service - " + name + " \n On host -" + host + "\n is down and Refused Connection");
        } else if (-2 == statusCode) {
            slackModel.setText("Service - " + name + " \n On Host -" + host + "\n connected but timed out after 5 seconds");
        } else
            slackModel.setText("Service - " + name + " \n Failed on Host-" + host + "\n with status code of -" + statusCode);
        slackModel.setIcon_emoji(":interrobang:");
        SlackUtil.sendMessage(slackModel);
    }

    private void setHeaders(HttpRequestBase request, List<HeaderModel> headers) {
        for (HeaderModel header : headers) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }

    @Override
    public void run() {
        checkServiceStatus();
    }
}
