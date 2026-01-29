package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.CreateOrderRequest;
import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.OrderResponse;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.dto.PricePreviewResponse;
import com.tonyghouse.restaurant_service.entity.Branch;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.BranchRepository;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import com.tonyghouse.restaurant_service.repo.OrderItemRepository;
import com.tonyghouse.restaurant_service.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    OrderPricingService pricingService;

    @Mock
    MenuItemRepository menuItemRepository;

    @Mock
    ComboRepository comboRepository;

    @Mock
    BranchRepository branchRepository;

    @Mock
    Clock clock;

    @InjectMocks
    OrderServiceImpl service;

    @BeforeEach
    void setup() {
        Mockito.when(clock.instant())
                .thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    }


    @Test
    void create_branchNotFound() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBranchId(UUID.randomUUID());
        req.setItems(List.of());

        Mockito.when(branchRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.create(req));
    }

    @Test
    void get_notFound() {
        Mockito.when(orderRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.get(UUID.randomUUID()));
    }

    @Test
    void preview_success() {
        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(new BigDecimal("300"));

        Mockito.when(pricingService.calculate(Mockito.any()))
                .thenReturn(breakdown);

        PricePreviewResponse res =
                service.preview(new CreateOrderRequest());

        assertEquals(new BigDecimal("300"),
                res.getBreakdown().getGrandTotal());
    }
}
