package com.chromeos.playtool.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAction implements Serializable {

    private Integer agentID;

    private String type;

    private Integer dx;

    private Integer dy;
}
