package com.chromeos.playtool.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotOrActionResult {
    private Integer agentId;
    private Integer dx;
    private Integer dy;
}
