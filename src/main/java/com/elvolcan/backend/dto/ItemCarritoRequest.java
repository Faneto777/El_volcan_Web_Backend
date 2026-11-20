package com.elvolcan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemCarritoRequest(
        @NotBlank String productoId,
        @NotNull @Positive Integer cantidad) {
}
