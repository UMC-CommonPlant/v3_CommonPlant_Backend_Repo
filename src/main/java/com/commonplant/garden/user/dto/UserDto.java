package com.commonplant.garden.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDto {
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class join{
        private String email;
        private  String name;
        private String provider;
    }
}
