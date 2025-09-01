package com.example.productos.domain;

import com.example.productos.domain.Producto;
import com.example.productos.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoServiceIntegrationTest {
    @Autowired
    private ProductoService service;

    @Test
    @DisplayName("Crear y obtener un producto por ID desde el servicio")
    void createAndGet() {
        Producto saved = service.crear("Monitor", new BigDecimal("500.00"), 3);

        Producto found = service.obtenerPorId(saved.getId());

        assertThat(found.getNombre()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("Lanza NotFoundException al buscar un ID inexistente")
    void notFound() {
        assertThrows(RuntimeException.class, () -> service.obtenerPorId(999999L));
    }
}