package com.tonyghouse.restaurant_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tonyghouse.restaurant_service.dto.MenuResponse;
import com.tonyghouse.restaurant_service.entity.Menu;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class MenuMapper {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MenuMapper() {
    }

    public static MenuResponse toResponse(Menu menu) {
        MenuResponse response = new MenuResponse();
        response.setId(menu.getId());
        response.setBranchId(menu.getBranch().getId());
        response.setMenuType(menu.getMenuType());
        response.setValidFrom(menu.getValidFrom());
        response.setValidTo(menu.getValidTo());
        response.setActive(menu.isActive());
        response.setCreatedAt(menu.getCreatedAt());
        return response;
    }
    
    public static MenuResponse fromJson(String cached) {
        try {
            return OBJECT_MAPPER.readValue(cached, MenuResponse.class);
        } catch (Exception e) {
            throw new RestoRestaurantException("Failed to deserialize MenuResponse from cache", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String toJson(MenuResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RestoRestaurantException("Failed to serialize MenuResponse for cache", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static List<MenuResponse> listFromJson(String cached) {
        try {
            return OBJECT_MAPPER.readValue(
                    cached,
                    new TypeReference<List<MenuResponse>>() {}
            );
        } catch (Exception e) {
            throw new RestoRestaurantException("Failed to deserialize MenuResponse list from cache", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String listToJson(List<MenuResponse> menus) {
        try {
            return OBJECT_MAPPER.writeValueAsString(menus);
        } catch (JsonProcessingException e) {
            throw new RestoRestaurantException("Failed to serialize MenuResponse list for cache", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
