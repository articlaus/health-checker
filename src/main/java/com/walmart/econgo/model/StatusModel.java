package com.walmart.econgo.model;

import lombok.Data;

/**
 * Created by: Ganbat Bayar
 * On: 5/19/2019
 * Project: ServiceStatusChecker
 */
@Data
public class StatusModel {
    String status;
    String serviceCount;
    String hostCount;
    String lastRun;
}
