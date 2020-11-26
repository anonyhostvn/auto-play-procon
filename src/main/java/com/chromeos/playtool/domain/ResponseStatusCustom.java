package com.chromeos.playtool.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseStatusCustom implements Serializable {
    private static final long serialVersionUID = 1216664062736095390L;

    private String code;

    private String message;
}
