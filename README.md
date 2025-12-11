# Task Management System

Sistema de gestión de tareas desarrollado en Java 17, Spring Boot,
PostgreSQL y Thymeleaf. Incluye autenticación con Spring Security,
manejo de usuarios, roles, permisos y CRUD completo de tareas.

## Características principales

-   Registro e inicio de sesión de usuarios\
-   Roles: Usuario y Administrador\
-   Permisos: READ, CREATE, UPDATE, DELETE\
-   CRUD de tareas\
-   Cambio de estado de tareas (Pendiente / Completada)\
-   Panel de administración de usuarios (solo ADMIN)\
-   Validaciones con Jakarta Validation\
-   Persistencia en PostgreSQL\
-   Vistas Thymeleaf

## Requisitos previos

-   Java 17 o superior\
-   PostgreSQL 14 o superior\
-   IntelliJ IDEA (Community o Ultimate)\
-   Maven

## Configuración del entorno

Archivo externo: **task-manager-env.txt**

Contenido:

    DB_HOST=localhost
    DB_NAME=task_management
    DB_PASSWORD=
    DB_PORT=5432
    DB_USER=

### Configuración en Windows

1.  Abrir variables de entorno\
2.  Crear variable:
    -   **Nombre:** TASK_MANAGER_ENV_FILE\
    -   **Valor:** ruta completa al archivo task-manager-env.txt

Ejemplo:

    C:\Users\TuUsuario\Documents\task-management-system\task-manager-env.txt

3.  Reiniciar IntelliJ IDEA

## Configuración de PostgreSQL

Crear la base de datos:

    CREATE DATABASE task_management;

## Ejecución del proyecto

1.  Abrir IntelliJ IDEA\
2.  Abrir carpeta del proyecto\
3.  Ejecutar clase:

```{=html}
<!-- -->
```
    TaskManagementSystemApplication.java

Servidor disponible en:

    http://localhost:8080/

## Credenciales iniciales

Crear administrador manualmente:

    INSERT INTO users (nombre, apellido, email, password, role, created_at, enabled)
    VALUES ('Admin', 'Master', 'admin@admin.com',
    '$2a$10$kl9iBMYCnfQbhv6p1OoEEuZ3EyoC7YkSBRgM2Ok3z86BdFkkeFNm2',
    'ADMIN', NOW(), true);

Contraseña: **admin123**

## Estructura del proyecto

    task-management-system/
     ├── src/main/java/sys/com/
     │     ├── controller/
     │     ├── service/
     │     ├── model/
     │     ├── dto/
     │     ├── repository/
     │     └── TaskManagementSystemApplication.java
     ├── src/main/resources/
     │     ├── templates/
     │     └── application.properties
     ├── task-manager-env.txt
     └── pom.xml

## Licencia

Uso académico.
