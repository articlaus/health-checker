package com.walmart.econgo.util;


import com.walmart.econgo.model.HeaderModel;
import com.walmart.econgo.model.ServiceModel;
import com.walmart.econgo.model.enums.MethodEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;

import static com.walmart.econgo.util.helper.RequestHelper.*;


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
            int timeout = model.getTimeoutSec();
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
            Integer healthyCount = hosts.length;
            URIBuilder builder;
            for (String host : hosts) {
                builder = new URIBuilder();
                builder.setScheme("http").setHost(host).setPath(model.getUrl());

                Optional<List<HeaderModel>> params = Optional.ofNullable(model.getParams());
                Optional<List<HeaderModel>> headers = Optional.ofNullable(model.getHeaders());
                if (params.isPresent())
                    for (HeaderModel param : params.get()) {
                        builder.setParameter(param.getKey(), param.getValue());
                    }
                HttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 400, "Empty Resp");
                try {
                    if (model.getMethod().equals(MethodEnum.GET)) {
                        HttpGet req = new HttpGet(builder.build());
                        headers.ifPresent(x -> setHeaders(req, x));
                        response = httpClient.execute(req);
                    } else {
                        HttpEntityEnclosingRequestBase req;
                        if (model.getMethod().equals(MethodEnum.PUT))
                            req = new HttpPut(builder.build());
                        else
                            req = new HttpPost(builder.build());
                        headers.ifPresent(x -> setHeaders(req, x));
                        StringEntity body = new StringEntity(new String(Base64.decodeBase64(model.getBody())));
                        req.setEntity(body);
                        response = httpClient.execute(req);
                    }
                    boolean flag = false;
                    for (Integer status : model.getStatus())
                        if (response.getStatusLine().getStatusCode() == status)
                            flag = true;

                    if (!flag) {
                        handleError(model, host, response);
                        healthyCount--;
                    } else
                        log.info("Service - " + model.getName() + " is healthy on host - " + host);
                } catch (HttpHostConnectException eh) {
                    response.setStatusCode(1);
                    handleError(model, host, response);
                    healthyCount--;
                } catch (ConnectTimeoutException | SocketTimeoutException eh) {
                    response.setStatusCode(2);
                    handleError(model, host, response);
                    healthyCount--;
                }
                sendReport(model, healthyCount, hosts.length);
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    @Override
    public void run() {
        checkServiceStatus();
    }
}
