package com.demokratica.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
    En Spring añadimos la anotación @Entity a todas las clases que queramos guardar en la BD.
    Podemos especificar el nombre de la tabla en caso de que el nombre de nuestra clase y el de la tabla
    en la BD no sea el mismo. Esto lo tuve que usar en Postgres una vez, porque por defecto Spring usa
    el nombre en minúsculas de la clase como nombre de la tabla, pero en Postgres user es una palabra
    reservada y causa errores al tratar de llamar una tabla así y luego consultarla
 */
@Entity
@Table(name = "users")
public class User {
    
    /*
        Hay anotaciones para definir cuál es la llave primaria, definir relaciones uno a muchos y muchos
        a muchos, añadir validaciones (como por ejemplo, longitudes mínimas de 20 carácteres o máximas de 50),
        definir "estrategias" para autogenerar la llave primaria, definir el nombre de la columna (parecido al
        caso de las tablas arriba, con @Column(name = "nuevo nombre") ), entre muchos otros
        
        Para más información buscar "Hibernate Annotations"
    */

    //NOTA: los atributos DEBEN SER PRIVADOS y se deben establecer Getters y Setters para cada uno. Esto
    //se puede hacer de forma automática con el IDE
    @Id
    private String username;
    private String password;
    private Boolean enabled;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
