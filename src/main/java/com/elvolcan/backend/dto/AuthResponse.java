package com.elvolcan.backend.dto;

public record AuthResponse(String token, String nombre, String email, String rol) {}