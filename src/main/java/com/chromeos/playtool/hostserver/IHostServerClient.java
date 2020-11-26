package com.chromeos.playtool.hostserver;

import com.chromeos.playtool.common.model.GameInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hostServerClient", url = "${com.uet.procon.host}")
public interface IHostServerClient {

    @GetMapping("${com.uet.procon.allmatches.endpoint}")
    List<GameInfo> getAllMatches(
            @RequestHeader(name = "Authorization") String token
    );

}
