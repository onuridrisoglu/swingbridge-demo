package com.example.dashboard;

/** Number of purchase orders in a given status. */
public record OrderStatusCount(String status, int count) {
}
