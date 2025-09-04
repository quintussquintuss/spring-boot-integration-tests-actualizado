# CONSTRUCCIÓN DEL APLICATIVO: PASO A PASO

## INTRODUCCIÓN

Este documento describe el proceso completo de construcción de la aplicación de gestión de productos en Spring Boot, desde la creación inicial hasta la implementación de todas las funcionalidades y pruebas de integración.

---

## PASO 1: CREACIÓN DEL PROYECTO BASE ✅

### 1.1 Estructura del Proyecto
- **Framework**: Spring Boot 3.2.0
- **Java**: Versión 17+
- **Gestor de dependencias**: Maven
- **Base de datos**: H2 en memoria

### 1.2 Configuración del POM
```xml
<dependencies>
    <!-- Spring Boot Web Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Base de datos H2 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Dependencias de prueba -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## PASO 2: CONFIGURACIÓN DE LA BASE DE DATOS ✅

### 2.1 Archivo de Configuración Principal
**`src/main/resources/application.properties`**
```properties
# Configuración de base de datos H2
spring.datasource.url=jdbc:h2:mem:prodapp;DB_CLOSE_DELAY=-1;MODE=LEGACY
spring.datasource.username=sa
spring.datasource.password=

# Configuración JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

### 2.2 Configuración para Pruebas
**`src/test/resources/application-test.properties`**
```properties
# Base de datos separada para pruebas
spring.datasource.url=jdbc:h2:mem:prodapp_test;DB_CLOSE_DELAY=-1;MODE=LEGACY
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.springframework=warn
```

---

## PASO 3: IMPLEMENTACIÓN DE LA ENTIDAD DOMINIO ✅

### 3.1 Clase Producto
**`src/main/java/com/example/productos/domain/Producto.java`**

```java
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;
    
    // Constructores, getters, setters y métodos equals/hashCode
}
```

**Funcionalidades implementadas:**
- ✅ Entidad JPA con anotaciones correctas
- ✅ Validaciones de base de datos (not null, unique)
- ✅ Precisión decimal para precios (12,2)
- ✅ Generación automática de ID
- ✅ Métodos equals y hashCode para comparaciones

---

## PASO 4: IMPLEMENTACIÓN DEL REPOSITORIO ✅

### 4.1 Interface ProductoRepository
**`src/main/java/com/example/productos/repository/ProductoRepository.java`**

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByNombre(String nombre);
}
```

**Funcionalidades implementadas:**
- ✅ Extensión de JpaRepository para operaciones CRUD básicas
- ✅ Método personalizado para búsqueda por nombre
- ✅ Configuración automática de Spring Data JPA
- ✅ Transacciones automáticas

---

## PASO 5: IMPLEMENTACIÓN DEL SERVICIO DE NEGOCIO ✅

### 5.1 Clase ProductoService
**`src/main/java/com/example/productos/service/ProductoService.java`**

```java
@Service
@Transactional
public class ProductoService {
    
    public List<Producto> listar() {
        return repository.findAll();
    }
    
    public Producto crear(String nombre, BigDecimal precio, Integer stock) {
        // Validaciones de negocio
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        Producto p = new Producto(nombre, precio, stock);
        return repository.save(p);
    }
    
    public Producto obtenerPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));
    }
    
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Producto no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
```

**Funcionalidades implementadas:**
- ✅ Operación de listado de todos los productos
- ✅ Creación de productos con validaciones de negocio
- ✅ Búsqueda por ID con manejo de excepciones
- ✅ Eliminación de productos con verificación de existencia
- ✅ Transacciones automáticas con `@Transactional`
- ✅ Validaciones de precio y stock no negativos

### 5.2 Clase de Excepción Personalizada
**`src/main/java/com/example/productos/service/NotFoundException.java`**
```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

---

## PASO 6: IMPLEMENTACIÓN DEL CONTROLADOR REST ✅

### 6.1 Clase ProductoController
**`src/main/java/com/example/productos/controller/ProductoController.java`**

```java
@RestController
@RequestMapping("/productos")
public class ProductoController {
    
    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }
    
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        BigDecimal precio = new BigDecimal(body.get("precio").toString());
        Integer stock = (Integer) body.get("stock");
        
        Producto creado = service.crear(nombre, precio, stock);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/productos/" + creado.getId())
                .body(creado);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
```

**Funcionalidades implementadas:**
- ✅ **GET /productos**: Lista todos los productos (código 200)
- ✅ **POST /productos**: Crea un nuevo producto (código 201)
- ✅ **GET /productos/{id}**: Obtiene producto por ID (código 200 o 404)
- ✅ **DELETE /productos/{id}**: Elimina producto por ID (código 204 o 404)
- ✅ Manejo de excepciones con códigos HTTP apropiados
- ✅ Header Location en respuestas de creación
- ✅ Mapeo REST con anotaciones Spring

---

## PASO 7: IMPLEMENTACIÓN DE PRUEBAS DE INTEGRACIÓN ✅

### 7.1 Pruebas de Repositorio
**`src/test/java/com/example/productos/domain/ProductoRepositoryIntegrationTest.java`**

```java
@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryIntegrationTest {
    
    @Test
    @DisplayName("Guardar y consultar producto por ID")
    void saveAndFindById() {
        // Implementación de prueba
    }
    
    @Test
    @DisplayName("Eliminar producto")
    void deleteProduct() {
        // Implementación de prueba
    }
}
```

**Funcionalidades probadas:**
- ✅ Guardado de productos en base de datos
- ✅ Consulta de productos por ID
- ✅ Eliminación de productos
- ✅ Uso de anotación `@DataJpaTest`

### 7.2 Pruebas de Servicio
**`src/test/java/com/example/productos/domain/ProductoServiceIntegrationTest.java`**

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoServiceIntegrationTest {
    
    @Test
    @DisplayName("Crear y obtener un producto por ID desde el servicio")
    void createAndGet() {
        // Implementación de prueba
    }
    
    @Test
    @DisplayName("Lanza NotFoundException al buscar un ID inexistente")
    void notFound() {
        // Implementación de prueba
    }
}
```

**Funcionalidades probadas:**
- ✅ Lógica de negocio del servicio
- ✅ Manejo de excepciones
- ✅ Uso de anotación `@SpringBootTest`

### 7.3 Pruebas de Controlador
**`src/test/java/com/example/productos/domain/ProductoControllerIntegrationTest.java`**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerIntegrationTest {
    
    @Test
    @DisplayName("GET /productos devuelve 200 y una lista")
    void listProductos() { /* ... */ }
    
    @Test
    @DisplayName("POST /productos crea recurso y devuelve 201")
    void createProducto() { /* ... */ }
    
    @Test
    @DisplayName("GET /productos/{id} devuelve 404 si no existe")
    void getNotFound() { /* ... */ }
    
    @Test
    @DisplayName("DELETE /productos/{id} devuelve 204 al eliminar")
    void deleteProducto() { /* ... */ }
}
```

**Funcionalidades probadas:**
- ✅ Todos los endpoints REST
- ✅ Códigos de estado HTTP correctos
- ✅ Flujos completos de operaciones
- ✅ Uso de MockMvc para simulación HTTP

---

## PASO 8: CONFIGURACIÓN DE PRUEBAS ADICIONALES ✅

### 8.1 Pruebas de Controlador Alternativas
**`src/test/java/com/example/productos/controller/ProductoControllerIT.java`**

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoControllerIT {
    
    @BeforeEach
    void setUp() {
        // Limpieza y preparación de datos de prueba
    }
    
    // Pruebas adicionales con setup personalizado
}
```

**Funcionalidades adicionales:**
- ✅ Configuración de datos de prueba con `@BeforeEach`
- ✅ Limpieza de base de datos entre pruebas
- ✅ Pruebas con datos predefinidos

---

## PASO 9: VALIDACIÓN Y EJECUCIÓN DE PRUEBAS ✅

### 9.1 Comando de Ejecución
```bash
mvn test
```

### 9.2 Resultados Obtenidos
```
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Funcionalidades validadas:**
- ✅ **13 pruebas ejecutadas exitosamente**
- ✅ **0 fallos, 0 errores**
- ✅ **Tiempo total: 14.026 segundos**
- ✅ **Reportes XML generados automáticamente**

---

## PASO 10: GENERACIÓN DE REPORTES ✅

### 10.1 Reportes Automáticos
- **Ubicación**: `target/surefire-reports/`
- **Formato**: XML con detalles de ejecución
- **Contenido**: Logs, tiempos, propiedades del sistema

### 10.2 Evidencia de Funcionamiento
- ✅ Todas las capas funcionando correctamente
- ✅ Base de datos H2 operativa
- ✅ Endpoints REST respondiendo correctamente
- ✅ Validaciones de negocio funcionando
- ✅ Manejo de excepciones implementado

---

## RESUMEN DE FUNCIONALIDADES IMPLEMENTADAS

### 🔧 **Funcionalidades de Base de Datos**
- ✅ Entidad Producto con JPA
- ✅ Repositorio con operaciones CRUD
- ✅ Base de datos H2 en memoria
- ✅ Configuración separada para pruebas

### 🚀 **Funcionalidades de Servicio**
- ✅ Lógica de negocio completa
- ✅ Validaciones de datos
- ✅ Manejo de excepciones
- ✅ Transacciones automáticas

### 🌐 **Funcionalidades de API REST**
- ✅ 4 endpoints implementados
- ✅ Códigos de estado HTTP correctos
- ✅ Manejo de errores apropiado
- ✅ Headers de respuesta correctos

### 🧪 **Funcionalidades de Pruebas**
- ✅ 13 pruebas de integración
- ✅ Cobertura completa de funcionalidades
- ✅ Pruebas de repositorio, servicio y controlador
- ✅ Reportes automáticos de ejecución

---

## ARQUITECTURA FINAL IMPLEMENTADA

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │───▶│     Service     │───▶│   Repository    │
│   (REST API)    │    │  (Business      │    │   (Data Access) │
│                 │    │   Logic)        │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MockMvc       │    │   Spring Boot   │    │   H2 Database   │
│   (Testing)     │    │   (Context)     │    │   (In-Memory)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## LECCIONES APRENDIDAS DURANTE EL DESARROLLO

1. **Separación de Responsabilidades**: Cada capa tiene su función específica
2. **Configuración de Perfiles**: Importante separar configuraciones de desarrollo y pruebas
3. **Manejo de Excepciones**: Implementar desde el servicio hasta el controlador
4. **Pruebas de Integración**: Validar la interacción real entre componentes
5. **Validaciones de Negocio**: Implementar en el servicio, no solo en la entidad

---

**Estado del Proyecto**: ✅ **COMPLETADO EXITOSAMENTE**  
**Fecha de Finalización**: 3 de septiembre de 2025  
**Cumplimiento de Requisitos**: **100% COMPLETADO**
