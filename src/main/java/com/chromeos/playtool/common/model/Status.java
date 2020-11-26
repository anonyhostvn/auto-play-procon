package com.chromeos.playtool.common.model;

import lombok.Data;

@Data
public class Status {

    private String status;

    public Status(String status) {
        this.status = status;
    }
}
