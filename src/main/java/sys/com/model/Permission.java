package sys.com.model;

public enum Permission {
    READ("Leer tareas"),
    CREATE("Crear tareas"),
    UPDATE("Actualizar tareas"),
    DELETE("Eliminar tareas");

    private final String displayName;

    Permission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
