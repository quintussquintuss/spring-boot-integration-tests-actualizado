# Reporte breve — Pruebas de integración

**Alcance:** Se validó la integración entre capas *Repositorio, Servicio y Controlador* sobre BD H2 en memoria, cubriendo crear, consultar, listar y eliminar productos; y el manejo de `404 Not Found`.

## Anotaciones y justificación
- `@DataJpaTest`: arranca solo los componentes JPA y usa un `EntityManager`/`DataSource` en memoria. Ideal para probar `ProductoRepository` sin levantar toda la app.
- `@SpringBootTest`: levanta el contexto completo para validar reglas de negocio del `ProductoService` y wiring real de beans.
- `@AutoConfigureMockMvc`: configura `MockMvc` para probar el controlador REST sin servidor externo, validando códigos 200/201/404/204 y cabecera `Location`.

## Diseño de pruebas
- **Repositorio**: guardar, consultar por id, eliminar.
- **Servicio**: crear y obtener por id, y excepción al no encontrar.
- **Controlador**: `GET /productos`, `POST /productos`, `GET /productos/{id}` (404), `DELETE /productos/{id}` (204).

## Datos de prueba
- Generados en cada método con objetos `Producto`; el perfil `test` usa `ddl-auto=create-drop` para aislar.

## Lecciones aprendidas
- Separar niveles de pruebas reduce el tiempo de diagnóstico.
- `MockMvc` permite validar contratos HTTP de forma rápida.
- Perfiles y H2 garantizan reproducibilidad sin depender de infra externa.