package com.walmart.econgo.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.walmart.econgo.model.ServiceModel;
import com.walmart.econgo.model.StatusModel;
import com.walmart.econgo.util.RequestRunnable;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Ganbat Bayar
 * On: 5/18/2019
 * Project: ServiceStatusChecker
 */
@Service
@Log4j2
public class HealthService {

    private List<ServiceModel> models = new ArrayList<>();
    private Instant lastRun = Instant.now();

    @PostConstruct
    private void init() {
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(System.getProperty("user.dir") + "\\service.json"));
            ServiceModel[] serviceArrays = gson.fromJson(reader, ServiceModel[].class);
            models = Arrays.asList(serviceArrays);
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    public ResponseEntity getInfo() {
        StatusModel model = new StatusModel();
        model.setStatus("Healthy");
        model.setServiceCount("Service count - " + models.size());
        model.setLastRun(lastRun.toString());
        return ResponseEntity.ok(model);
    }


    @Scheduled(fixedDelay = 300000)
    private void checkHealth() {
        for (ServiceModel model : models) {
            new Thread(new RequestRunnable(model)).start();
        }
    }
}