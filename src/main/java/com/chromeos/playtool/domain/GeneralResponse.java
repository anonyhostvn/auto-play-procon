package com.chromeos.playtool.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {
    private static final long serialVersionUID = 1L;
    private ResponseStatusCustom status;
    private T data;
}
