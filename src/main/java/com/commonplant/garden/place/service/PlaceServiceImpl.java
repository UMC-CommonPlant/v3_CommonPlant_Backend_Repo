package com.commonplant.garden.place.service;

import com.commonplant.garden.belong.entity.Belong;
import com.commonplant.garden.belong.entity.BelongRepository;
import com.commonplant.garden.common.exception.BusinessException;
// import com.commonplant.garden.memo.entity.Memo;
// import com.commonplant.garden.memo.entity.MemoRepository;
import com.commonplant.garden.place.dto.PlaceDto;
import com.commonplant.garden.place.entity.Place;
import com.commonplant.garden.place.entity.PlaceRepository;
import com.commonplant.garden.place.exception.PlaceErrorCode;
// import com.commonplant.garden.plant.entity.Plant;
// import com.commonplant.garden.plant.entity.PlantRepository;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final BelongRepository belongRepository;
    // [TODO]: Plant, Memo 도메인 연동 후 해제
    // private final PlantRepository plantRepository;
    // private final MemoRepository memoRepository;

    private final UserServiceImpl userService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public String create(String nanoId, PlaceDto.createPlaceReq req, MultipartFile image) {
        User user = userService.findActiveUserByNanoId(nanoId);

        String newCode = generateUniqueCode();

        // [TODO]: Live Weather API

        String imageKey = uploadImageIfPresent(nanoId, image);

        // [TODO]: Place Info: gridX, gridY
        Place place = Place.builder()
                .name(req.getName())
                .address(req.getAddress())
                .code(newCode)
                .gridX("126")
                .gridY("37")
                .imgUrl(imageKey)
                .owner(user)
                .build();

        placeRepository.save(place);

        Belong belong = Belong.builder().user(user).place(place).build();
        belongRepository.save(belong);

        return newCode;
    }

    @Override
    @Transactional
    public PlaceDto.getPlaceRes getPlace(String nanoId, String code) {
        User user = userService.findActiveUserByNanoId(nanoId);

        belongUserOnPlace(user.getNanoId(), code);
        Place place = getPlaceByCode(code);

        List<PlaceDto.getPlaceResUser> userList =
                belongRepository.getUserListByPlaceCode(code)
                        .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND))
                        .stream()
                        .map(u -> new PlaceDto.getPlaceResUser(u.getName(), u.getImgUrl()))
                        .collect(Collectors.toList());

        PlaceDto.getPlaceRes res = PlaceDto.getPlaceRes.builder()
                .name(place.getName())
                .address(place.getAddress())
                .code(place.getCode())
                .isOwner(false)
                .imgUrl(resolveImageUrl(place.getImgUrl()))
                .userList(userList)
                .build();

        if (place.getOwner().getUserIdx().equals(user.getUserIdx())) {
            res.setOwner(true);
        }

        return res;
    }

    @Override
    @Transactional
    public List<PlaceDto.getPlaceListRes> getPlaceList(String nanoId) {
        User user = userService.findActiveUserByNanoId(nanoId);

        List<Place> places = placeRepository.findAllByOwner(user);
        List<PlaceDto.getPlaceListRes> placeList = new ArrayList<>();

        for (Place place : places) {
            String member = belongRepository.countUserByPlace(place);
            // [TODO]: 장소에 속한 식물 수 반환
            // String plant = plantRepository.countPlantsByPlace(place);
            String plant = "0";
            placeList.add(0, new PlaceDto.getPlaceListRes(place, member, plant, resolveImageUrl(place.getImgUrl())));
        }
        return placeList;
    }

    @Override
    public PlaceDto.getPlaceGridRes getPlaceGrid(String nanoId, String code) {
        User user = userService.findActiveUserByNanoId(nanoId);

        belongUserOnPlace(user.getNanoId(), code);
        Place place = getPlaceByCode(code);
        return new PlaceDto.getPlaceGridRes(place.getGridX(), place.getGridY());
    }


    @Override
    public List<PlaceDto.getPlaceBelongUser> getPlaceBelongUser(String nanoId) {
        User user = userService.findActiveUserByNanoId(nanoId);

        List<Belong> belongs = belongRepository.getPlaceBelongUser(user.getNanoId());
        List<PlaceDto.getPlaceBelongUser> belongList = new ArrayList<>();
        for (Belong b : belongs) {
            PlaceDto.getPlaceBelongUser belongUser = new PlaceDto.getPlaceBelongUser(
                    b.getPlace().getCode(),
                    b.getPlace().getName(),
                    resolveImageUrl(b.getPlace().getImgUrl()));
            belongList.add(belongUser);
        }
        return belongList;
    }


    @Override
    @Transactional
    public PlaceDto.updatePlaceRes updatePlace(String nanoId, String code, PlaceDto.updatePlaceReq req, MultipartFile image) {
        User user = userService.findActiveUserByNanoId(nanoId);

        Place place = getPlaceByCode(code);
        belongUserOnPlace(user.getNanoId(), code);

        validateUpdateRequest(place, req, image);
        String imageKey = resolveUpdatedImageKey(nanoId, place.getImgUrl(), req, image);

        // [TODO]: Place Info: gridX, gridY

        Place newPlaceInfo = Place.builder()
                .name(req.getName())
                .address(req.getAddress())
                .code(code)
                .gridX("126")
                .gridY("37")
                .imgUrl(imageKey)
                .owner(place.getOwner())
                .build();
        newPlaceInfo.setPlaceIdx(place.getPlaceIdx());
        placeRepository.save(newPlaceInfo);

        return new PlaceDto.updatePlaceRes(
                newPlaceInfo.getCode(),
                newPlaceInfo.getName(),
                newPlaceInfo.getAddress(),
                resolveImageUrl(newPlaceInfo.getImgUrl())
        );
    }

    @Override
    @Transactional
    public void deletePlace(String nanoId, String code) {
        User user = userService.findActiveUserByNanoId(nanoId);

        Place place = getPlaceByCode(code);
        Long placeIdx = place.getPlaceIdx();

        Optional<Belong> belongInfo =
                belongRepository.getBelongByUserAndPlace(user.getNanoId(), place.getCode());
        if (belongInfo.isEmpty()) {
            throw new BusinessException(PlaceErrorCode.USER_NOT_ON_PLACE);
        }

        // [TODO]: Plant, Memo 도메인 연동 후 해제
        // [TODO]: 장소에 속한 식물의 idx를 반환
        /*
        List<Long> plants = plantRepository.findAllByPlace(place).stream()
                .map(Plant::getPlantIdx)
                .collect(Collectors.toList());

        for (Long plantIdx : plants) {
            List<Memo> memos = memoRepository.findByPlantIdx(plantIdx);
            for (Memo memo : memos) {
                memoRepository.deleteById(memo.getMemoIdx());
            }
            plantRepository.deleteById(plantIdx);
        }
         */
        deleteImageIfPresent(nanoId, place.getImgUrl());
        placeRepository.deleteById(placeIdx);
    }

    // Helper Methods
    /*
        해당 장소에 속한 사용자 수 조회
     */
    @Override
    public void belongUserOnPlace(String nanoId, String code) {
        if (belongRepository.countUserOnPlace(nanoId, code) < 1) {
            throw new BusinessException(PlaceErrorCode.USER_NOT_ON_PLACE);
        }
    }

    /*
        nanoId 기준으로 사용자가 속한 place id 목록을 조회
     */
    @Override
    public List<Long> getPlaceIdsByUser(String nanoId) {
        User user = userService.findActiveUserByNanoId(nanoId);
        return belongRepository.getPlaceIdsByUser(user.getNanoId());
    }

    /*
        placeIdx 기준으로 place의 이름을 조회
     */
    @Override
    public String getPlaceNameById(Long placeId) {
        return placeRepository.findNameById(placeId)
                .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    /*
        placeIdx 기준으로 place의 주소를 조회
    */
    @Override
    public String getPlaceAddressById(Long placeId) {
        return placeRepository.findAddressById(placeId)
                .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    /*
        placeIdx 기준으로 place를 조회
     */
    @Override
    public Place getPlaceById(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    /*
        code 기준으로 place를 조회
     */
    @Override
    public Place getPlaceByCode(String code) {
        return placeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    /*
        Place Code 생성
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = RandomStringUtils.random(6, 33, 125, true, false);
        } while (placeRepository.existsByCode(code));
        return code;
    }

    private String uploadImageIfPresent(String nanoId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        return s3Service.uploadImage(nanoId, image).getKey();
    }

    private String resolveUpdatedImageKey(
            String nanoId,
            String existingImageKey,
            PlaceDto.updatePlaceReq request,
            MultipartFile image
    ) {
        if (hasFile(image)) {
            if (!StringUtils.hasText(existingImageKey)) {
                return s3Service.uploadImage(nanoId, image).getKey();
            }
            return s3Service.updateImage(nanoId, existingImageKey, image).getKey();
        }

        if (!StringUtils.hasText(existingImageKey)) {
            validateAbsentImageKey(request);
            return null;
        }

        if (hasSameImageKey(request, existingImageKey)) {
            return existingImageKey;
        }

        if (hasRequestedImageKey(request)) {
            throw new BusinessException(PlaceErrorCode.INVALID_IMAGE_KEY);
        }

        deleteImageIfPresent(nanoId, existingImageKey);
        return null;
    }

    private boolean hasFile(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    private boolean isEmptyFile(MultipartFile image) {
        return image == null || image.isEmpty();
    }

    private boolean hasRequestedImageKey(PlaceDto.updatePlaceReq request) {
        return request != null && StringUtils.hasText(request.getImageKey());
    }

    private boolean hasSameImageKey(PlaceDto.updatePlaceReq request, String existingImageKey) {
        return hasRequestedImageKey(request) && request.getImageKey().trim().equals(existingImageKey);
    }

    private void validateAbsentImageKey(PlaceDto.updatePlaceReq request) {
        if (hasRequestedImageKey(request)) {
            throw new BusinessException(PlaceErrorCode.INVALID_IMAGE_KEY);
        }
    }

    private void validateUpdateRequest(Place place, PlaceDto.updatePlaceReq request, MultipartFile image) {
        if (!StringUtils.hasText(place.getImgUrl()) && request == null && isEmptyFile(image)) {
            throw new BusinessException(PlaceErrorCode.NO_FIELDS_TO_UPDATE);
        }
    }

    private void deleteImageIfPresent(String nanoId, String imageKey) {
        if (StringUtils.hasText(imageKey)) {
            s3Service.deleteImage(nanoId, imageKey);
        }
    }

    private String resolveImageUrl(String imageKey) {
        if (!StringUtils.hasText(imageKey)) {
            return null;
        }
        return s3Service.getImageUrl(imageKey);
    }
}
