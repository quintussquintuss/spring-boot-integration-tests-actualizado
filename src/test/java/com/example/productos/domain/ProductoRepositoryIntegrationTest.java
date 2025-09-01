package com.example.productos.domain;

import com.example.productos.domain.Producto;
import com.example.productos.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryIntegrationTest {
    @Autowired
    private ProductoRepository repository;

    @Test
    @DisplayName("Guardar y consultar producto por ID")
    void saveAndFindById() {
        Producto p = new Producto();
        p.setNombre("Teclado");
        p.setPrecio(new BigDecimal("99.90"));
        p.setStock(10);

        Producto saved = repository.save(p);
        Optional<Producto> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Teclado");
    }

    @Test
    @DisplayName("Eliminar producto")
    void deleteProduct() {
        Producto p = new Producto();
        p.setNombre("Mouse");
        p.setPrecio(new BigDecimal("25.00"));
        p.setStock(5);
        Producto saved = repository.save(p);

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}