package com.walmart.econgo.model;

import com.walmart.econgo.model.enums.MethodEnum;
import lombok.Data;

import java.util.List;

/**
 * Created by: Ganbat Bayar
 * On: 5/18/2019
 * Project: ServiceStatusChecker
 */
@Data
public class ServiceModel {
    String name;
    String url;
    String hosts;
    List<HeaderModel> params;
    MethodEnum method;
    String body;
    List<HeaderModel> headers;
}
