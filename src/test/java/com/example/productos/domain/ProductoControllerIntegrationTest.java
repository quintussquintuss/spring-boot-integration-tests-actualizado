package com.example.productos.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @Test
    @DisplayName("GET /productos devuelve 200 y una lista")
    void listProductos() throws Exception {
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /productos crea recurso y devuelve 201")
    void createProducto() throws Exception {
        Producto body = new Producto();
        body.setNombre("Laptop");
        body.setPrecio(new BigDecimal("2500.00"));
        body.setStock(7);

        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /productos/{id} devuelve 404 si no existe")
    void getNotFound() throws Exception {
        mockMvc.perform(get("/productos/123456"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /productos/{id} devuelve 204 al eliminar")
    void deleteProducto() throws Exception {
        // Primero crear
        Producto body = new Producto();
        body.setNombre("Borrable");
        body.setPrecio(new BigDecimal("10.00"));
        body.setStock(1);

        String location = mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        // Extraer ID del Location
        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(delete("/productos/" + id))
                .andExpect(status().isNoContent());
    }
}