package com.chromeos.playtool.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestActionList implements Serializable {

    private List<RequestAction> actions;
}
