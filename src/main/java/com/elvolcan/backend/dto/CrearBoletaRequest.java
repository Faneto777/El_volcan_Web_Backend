package com.elvolcan.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearBoletaRequest(@NotEmpty List<@NotNull @Valid ItemCarritoRequest> items) {
}
