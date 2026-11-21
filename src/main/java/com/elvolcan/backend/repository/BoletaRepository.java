package com.elvolcan.backend.repository;

import com.elvolcan.backend.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findAllByUsuarioEmailOrderByFechaDesc(String email);
}
