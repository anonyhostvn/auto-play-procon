package com.chromeos.playtool.hostserver;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hostServerClient", url = "${com.uet.procon.host}")
public interface IHostServerClient {

    @GetMapping("${com.uet.procon.allmatches.endpoint}")
    List<GameInfo> getAllMatches(
            @RequestHeader(name = "Authorization") String token
    );

    @GetMapping("${com.uet.procon.allmatches.endpoint}/{matchId}")
    MapState getRecentMapState(
            @RequestHeader(name = "Authorization") String token,
            @PathVariable(name = "matchId") String matchId
    );

}
