package com.tonyghouse.restaurant_service.service;

import com.tonyghouse.restaurant_service.dto.OrderItemRequest;
import com.tonyghouse.restaurant_service.dto.PriceBreakdown;
import com.tonyghouse.restaurant_service.entity.Combo;
import com.tonyghouse.restaurant_service.entity.MenuItem;
import com.tonyghouse.restaurant_service.entity.Order;
import com.tonyghouse.restaurant_service.entity.OrderItem;
import com.tonyghouse.restaurant_service.exception.RestoRestaurantException;
import com.tonyghouse.restaurant_service.repo.ComboRepository;
import com.tonyghouse.restaurant_service.repo.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderPricingServiceImplTest {

    @Mock
    MenuItemRepository menuItemRepository;

    @Mock
    ComboRepository comboRepository;

    @InjectMocks
    OrderPricingServiceImpl service;

    @Test
    void calculate_itemAndCombo_success() {
        UUID itemId = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();

        MenuItem item = new MenuItem();
        item.setPrice(new BigDecimal("100"));

        Combo combo = new Combo();
        combo.setComboPrice(new BigDecimal("200"));

        Mockito.when(menuItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        Mockito.when(comboRepository.findById(comboId))
                .thenReturn(Optional.of(combo));

        OrderItemRequest r1 = new OrderItemRequest();
        r1.setItemId(itemId);
        r1.setItemType("ITEM");
        r1.setQuantity(2);

        OrderItemRequest r2 = new OrderItemRequest();
        r2.setItemId(comboId);
        r2.setItemType("COMBO");
        r2.setQuantity(1);

        PriceBreakdown res = service.calculate(List.of(r1, r2));

        assertEquals(new BigDecimal("400"), res.getItemsTotal());
        assertEquals(new BigDecimal("72.00"), res.getTax());
        assertEquals(new BigDecimal("472.00"), res.getGrandTotal());
    }

    @Test
    void calculate_invalidItemType() {
        OrderItemRequest req = new OrderItemRequest();
        req.setItemType("INVALID");
        req.setQuantity(1);

        assertThrows(RestoRestaurantException.class,
                () -> service.calculate(List.of(req)));
    }

    @Test
    void recalculateFromOrder_success() {
        Order order = new Order();

        OrderItem i1 = new OrderItem();
        i1.setUnitPrice(new BigDecimal("100"));
        i1.setQuantity(2);

        OrderItem i2 = new OrderItem();
        i2.setUnitPrice(new BigDecimal("50"));
        i2.setQuantity(1);

        order.setItems(List.of(i1, i2));

        PriceBreakdown res = service.recalculateFromOrder(order);

        assertEquals(new BigDecimal("250"), res.getItemsTotal());
        assertEquals(new BigDecimal("45.00"), res.getTax());
        assertEquals(new BigDecimal("295.00"), res.getGrandTotal());
    }

    @Test
    void recalculateFromOrder_noItems() {
        Order order = new Order();
        order.setItems(List.of());

        assertThrows(RestoRestaurantException.class,
                () -> service.recalculateFromOrder(order));
    }

    @Test
    void recalculateFromOrder_invalidItemData() {
        Order order = new Order();

        OrderItem item = new OrderItem();
        item.setUnitPrice(null);
        item.setQuantity(1);

        order.setItems(List.of(item));

        assertThrows(RestoRestaurantException.class,
                () -> service.recalculateFromOrder(order));
    }
}
