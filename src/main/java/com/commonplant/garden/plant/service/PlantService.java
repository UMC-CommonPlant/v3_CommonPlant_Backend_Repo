package com.commonplant.garden.plant.service;

import com.commonplant.garden.plant.dto.PlantRequest;
import com.commonplant.garden.plant.dto.PlantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlantService {
    PlantResponse.DeleteResponse deletePlant(String nanoId, String placeCode, Long plantId);

    PlantResponse.EditInfoResponse getPlantEditInfo(String nanoId, Long plantId);

    PlantResponse.EditInfoResponse updatePlant(
            String nanoId,
            String placeCode,
            Long plantId,
            PlantRequest.UpdateRequest request,
            MultipartFile image
    );

    PlantResponse.DetailResponse getPlant(String nanoId, Long plantId);

    PlantResponse.PlantListResponse getPlants(String nanoId, int page, int size);

    List<PlantResponse.PlantSummary> getPlantsByPlace(String nanoId, String placeCode);

    PlantResponse.CreateResponse createPlant(String nanoId, PlantRequest.CreateRequest request, MultipartFile image);
}
