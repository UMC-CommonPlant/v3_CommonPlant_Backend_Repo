package com.commonplant.garden.plant.service;

import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;

public interface PlantService {
    PlantResponse.PlantListResponse getPlants(String nanoId, int page, int size);

    PlantResponse.CreateResponse createPlant(String nanoId, PlantRequest.CreateRequest request);
}
