package com.example.dashboard;

import org.jspecify.annotations.NonNull;

/** Number of purchase orders in a given status. */
public record OrderStatusCount(@NonNull String status, int count) {
}
