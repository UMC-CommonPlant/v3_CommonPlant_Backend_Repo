package com.commonplant.garden.plant.controller;

import com.commonplant.garden.common.dto.JsonResponse;
import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import com.commonplant.garden.plant.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/plants")
public class PlantController {

    private final PlantService plantService;

    @PostMapping
    public ResponseEntity<JsonResponse> createPlant(
            @AuthenticationPrincipal String nanoId,
            @Valid @RequestBody PlantRequest.CreateRequest request
    ) {
        PlantResponse.CreateResponse response = plantService.createPlant(nanoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JsonResponse(true, 201, "createPlant", response));
    }
}
