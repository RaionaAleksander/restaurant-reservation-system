package com.aleksander.restaurant.reservation.specification;

import com.aleksander.restaurant.reservation.model.RestaurantTable;
import com.aleksander.restaurant.reservation.model.Zone;
import org.springframework.data.jpa.domain.Specification;

public class TableSpecification {

    public static Specification<RestaurantTable> hasCapacity(Integer capacity) {
        return (root, query, cb) -> capacity == null ? null : cb.equal(root.get("capacity"), capacity);
    }

    public static Specification<RestaurantTable> hasMinCapacity(Integer minCapacity) {
        return (root, query, cb) -> minCapacity == null ? null
                : cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
    }

    public static Specification<RestaurantTable> hasZone(Zone zone) {
        return (root, query, cb) -> zone == null ? null : cb.equal(root.get("zone"), zone);
    }

    public static Specification<RestaurantTable> nearWindow(Boolean nearWindow) {
        return (root, query, cb) -> nearWindow == null ? null : cb.equal(root.get("nearWindow"), nearWindow);
    }

    public static Specification<RestaurantTable> nearKidsZone(Boolean nearKidsZone) {
        return (root, query, cb) -> nearKidsZone == null ? null : cb.equal(root.get("nearKidsZone"), nearKidsZone);
    }

    public static Specification<RestaurantTable> quietCorner(Boolean quietCorner) {
        return (root, query, cb) -> quietCorner == null ? null : cb.equal(root.get("quietCorner"), quietCorner);
    }

    public static Specification<RestaurantTable> accessible(Boolean accessible) {
        return (root, query, cb) -> accessible == null ? null : cb.equal(root.get("accessible"), accessible);
    }
}