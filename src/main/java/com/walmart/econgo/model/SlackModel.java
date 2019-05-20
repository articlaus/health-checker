package com.walmart.econgo.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by: Ganbat Bayar
 * On: 5/19/2019
 * Project: ServiceStatusChecker
 */
@Data
public class SlackModel implements Serializable {
    String channel;
    String username;
    String text;
    String icon_emoji;
    String color;
    List<AttachmentModel> attachments;
}
