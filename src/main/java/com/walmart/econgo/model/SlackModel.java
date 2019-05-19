package com.walmart.econgo.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by: Ganbat Bayar
 * On: 5/19/2019
 * Project: ServiceStatusChecker
 */
@Data
public class SlackModel implements Serializable {
    String username;
    String text;
    String icon_emoji;
}
