package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.constants.OrderStatus;
import com.tonyghouse.restaurant_service.dto.*;
import com.tonyghouse.restaurant_service.entity.*;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import static org.mockito.Mockito.*;

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

    Instant fixedInstant = Instant.parse("2025-01-01T00:00:00Z");

    @BeforeEach
    void setup() {
        when(clock.instant()).thenReturn(fixedInstant);
    }

    @Test
    void create_branchNotFound() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBranchId(UUID.randomUUID());

        when(branchRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(RestoRestaurantException.class,
                () -> service.create(req));
    }

    @Test
    void create_success_itemFlow() {

        UUID branchId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Branch branch = new Branch();
        branch.setId(branchId);

        MenuItem menuItem = new MenuItem();
        menuItem.setId(itemId);
        menuItem.setName("Burger");
        menuItem.setPrice(new BigDecimal("100"));

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemType("ITEM");
        itemReq.setItemId(itemId);
        itemReq.setQuantity(2);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBranchId(branchId);
        req.setCustomerName("Tony");
        req.setCustomerPhone("999");
        req.setItems(List.of(itemReq));

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(new BigDecimal("200"));

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));
        when(menuItemRepository.findById(itemId))
                .thenReturn(Optional.of(menuItem));
        when(pricingService.calculate(any()))
                .thenReturn(breakdown);
        when(orderRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = service.create(req);

        assertEquals(OrderStatus.CREATED.toString(), res.getStatus());
        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void create_success_comboFlow() {

        UUID branchId = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();

        Branch branch = new Branch();
        branch.setId(branchId);

        Combo combo = new Combo();
        combo.setId(comboId);
        combo.setName("Family Combo");
        combo.setComboPrice(new BigDecimal("500"));

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemType("COMBO");
        itemReq.setItemId(comboId);
        itemReq.setQuantity(1);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBranchId(branchId);
        req.setItems(List.of(itemReq));

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(new BigDecimal("500"));

        when(branchRepository.findById(branchId))
                .thenReturn(Optional.of(branch));
        when(comboRepository.findById(comboId))
                .thenReturn(Optional.of(combo));
        when(pricingService.calculate(any()))
                .thenReturn(breakdown);
        when(orderRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(req);

        verify(orderItemRepository).save(any());
    }

    @Test
    void get_success() {

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("300"));
        order.setStatus(OrderStatus.ACCEPTED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        OrderResponse res = service.get(orderId);

        assertEquals(orderId, res.getOrderId());
    }

    @Test
    void get_notFound() {

        when(orderRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.get(UUID.randomUUID()));
    }

    @Test
    void preview_success() {

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.setGrandTotal(new BigDecimal("300"));

        when(pricingService.calculate(any()))
                .thenReturn(breakdown);

        PricePreviewResponse res =
                service.preview(new CreateOrderRequest());

        assertEquals(new BigDecimal("300"),
                res.getBreakdown().getGrandTotal());
    }
}
