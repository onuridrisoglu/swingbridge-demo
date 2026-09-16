package com.example.dashboard;

import org.jspecify.annotations.NonNull;

/** Units in stock for a single product category. */
public record CategoryStock(@NonNull String category, int units) {
}
