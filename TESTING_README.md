# Guía rápida de pruebas de integración

## Requisitos
- Java 17
- Maven 3.9+

## Perfiles
- `test`: usa H2 en memoria con `application-test.properties`.

## Ejecutar todas las pruebas
```bash
mvn -Dspring.profiles.active=test clean test
```

## Reportes JUnit
- Se generan en `target/surefire-reports`.
- Si usas IntelliJ o VS Code, puedes verlos desde la vista de pruebas.

## Cobertura (opcional)
Agrega JaCoCo al `pom.xml`:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```