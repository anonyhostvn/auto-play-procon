package com.chromeos.playtool.controllers;

import com.chromeos.playtool.domain.GeneralResponse;
import com.chromeos.playtool.domain.ResponseFactory;
import com.chromeos.playtool.models.request.SetTokenPlayToolRequest;
import com.chromeos.playtool.models.response.SetTokenPlayToolResponse;
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

}
