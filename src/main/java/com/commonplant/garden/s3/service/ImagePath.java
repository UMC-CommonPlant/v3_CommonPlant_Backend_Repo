package com.commonplant.garden.s3.service;

import java.util.regex.Pattern;

public record ImagePath(String directory) {

    private static final String ROOT = "images";
    private static final Pattern SAFE_SEGMENT = Pattern.compile("[A-Za-z0-9_-]+");

    public ImagePath {
        if (directory == null || directory.isBlank()
                || directory.startsWith("/")
                || directory.endsWith("/")
                || directory.contains("//")
                || directory.contains("..")) {
            throw new IllegalArgumentException("Invalid image directory");
        }
    }

    public static ImagePath place(String placeCode) {
        return new ImagePath(ROOT + "/" + segment("placeCode", placeCode));
    }

    public static ImagePath plant(String placeCode, Long plantId) {
        return new ImagePath(place(placeCode).directory() + "/plants/" + positiveId("plantId", plantId));
    }

    public static ImagePath memo(String placeCode, Long plantId, Long memoId) {
        return new ImagePath(plant(placeCode, plantId).directory()
                + "/memos/" + positiveId("memoId", memoId));
    }

    public static ImagePath userProfile(String nanoId) {
        return new ImagePath(ROOT + "/users/" + segment("nanoId", nanoId));
    }

    public static ImagePath legacy(String nanoId) {
        return new ImagePath(ROOT + "/" + segment("nanoId", nanoId));
    }

    public static ImagePath fromImageKey(String imageKey) {
        if (imageKey == null || !imageKey.startsWith(ROOT + "/")) {
            throw new IllegalArgumentException("Invalid image key");
        }

        int fileSeparatorIndex = imageKey.lastIndexOf('/');
        if (fileSeparatorIndex <= ROOT.length() || fileSeparatorIndex == imageKey.length() - 1) {
            throw new IllegalArgumentException("Invalid image key");
        }
        return new ImagePath(imageKey.substring(0, fileSeparatorIndex));
    }

    private static String segment(String name, String value) {
        if (value == null || !SAFE_SEGMENT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        return value;
    }

    private static long positiveId(String name, Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        return value;
    }
}
