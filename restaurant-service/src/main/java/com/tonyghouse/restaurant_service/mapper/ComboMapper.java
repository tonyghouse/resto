package com.tonyghouse.restaurant_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tonyghouse.restaurant_service.dto.ComboResponse;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ComboMapper {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static ComboResponse toResponse(Combo combo) {
        ComboResponse r = new ComboResponse();
        r.setId(combo.getId());
        r.setName(combo.getName());
        r.setDescription(combo.getDescription());
        r.setComboPrice(combo.getComboPrice());
        r.setActive(combo.isActive());
        r.setCreatedAt(combo.getCreatedAt());
        r.setItemIds(
                combo.getItems().stream()
                        .map(MenuItem::getId)
                        .toList()
        );
        return r;
    }

    public static String toJson(ComboResponse response) {
        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RestoRestaurantException("Failed to serialize ComboResponse", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ComboResponse fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, ComboResponse.class);
        } catch (Exception e) {
            throw new RestoRestaurantException("Failed to deserialize ComboResponse", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String toJsonList(List<ComboResponse> responses) {
        try {
            return OBJECT_MAPPER.writeValueAsString(responses);
        } catch (JsonProcessingException e) {
            throw new RestoRestaurantException("Failed to serialize ComboResponse list", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static List<ComboResponse> fromJsonList(String json) {
        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    new TypeReference<List<ComboResponse>>() {
                    }
            );
        } catch (Exception e) {
            throw new RestoRestaurantException("Failed to deserialize ComboResponse list", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
