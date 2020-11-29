package com.chromeos.playtool.hostserver;

import com.chromeos.playtool.common.model.GameInfo;
import com.chromeos.playtool.common.model.MapState;
import com.chromeos.playtool.common.model.RequestActionList;
import com.chromeos.playtool.domain.EmptyResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("${com.uet.procon.allmatches.endpoint}/{matchId}/action")
    EmptyResponseData sendActionToServer(
            @RequestHeader(name = "Authorization") String token,
            @PathVariable(name = "matchId") String matchId,
            @RequestBody RequestActionList requestActionList
    );

}
