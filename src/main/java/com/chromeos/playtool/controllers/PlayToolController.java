package com.chromeos.playtool.controllers;

import com.chromeos.playtool.domain.GeneralResponse;
import com.chromeos.playtool.domain.ResponseFactory;
import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.*;
import com.chromeos.playtool.service.IPlayToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/play-tool")
@Slf4j
public class PlayToolController {

    private final IPlayToolService iPlayToolService;

    @Autowired
    public PlayToolController(IPlayToolService iPlayToolService) {
        this.iPlayToolService = iPlayToolService;
    }

    @PostMapping("/set-token")
    public ResponseEntity<GeneralResponse<SetTokenPlayToolResponse>> setTokenToSystem(
            @RequestBody SetTokenPlayToolRequest setTokenPlayToolRequest
    ) {
        return ResponseFactory.success(iPlayToolService.setNewToken(setTokenPlayToolRequest));
    }

    @GetMapping("/set-current-game")
    public ResponseEntity<GeneralResponse<SetCurrentGameResponse>> setCurrentGame(
        @RequestParam(name = "matchId") Long matchId
    ) {
        return ResponseFactory.success(iPlayToolService.setCurrentGame(matchId));
    }

    @GetMapping("/get-token")
    public ResponseEntity<GeneralResponse<GetRecentTokenRequest>> getRecentToken() {
        return ResponseFactory.success(iPlayToolService.getRecentToken());
    }

    @GetMapping("/get-all-game")
    public ResponseEntity<GeneralResponse<GetAllGameResponse>> getAllGame() {
        return ResponseFactory.success(iPlayToolService.getAllGame());
    }

    @GetMapping("/update-state")
    public ResponseEntity<GeneralResponse<UpdateGameStateResponse>> updateGameState() {
        return ResponseFactory.success(iPlayToolService.updateGameState());
    }

}
