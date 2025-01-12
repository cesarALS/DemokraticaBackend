package com.demokratica.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/*
    En Spring añadimos la anotación @Entity a todas las clases que queramos guardar en la BD.
    Podemos especificar el nombre de la tabla en caso de que el nombre de nuestra clase y el de la tabla
    en la BD no sea el mismo. Esto lo tuve que usar en Postgres una vez, porque por defecto Spring usa
    el nombre en minúsculas de la clase como nombre de la tabla, pero en Postgres user es una palabra
    reservada y causa errores al tratar de llamar una tabla así y luego consultarla
 */
@Entity
@Table(name = "users")
//Este es un método de Lombok (un proyecto de Spring) que hace que no tengamos que definir getters y setters
//a mano
@Data 
public class User {
    
    /*
        Hay anotaciones para definir cuál es la llave primaria, definir relaciones uno a muchos y muchos
        a muchos, añadir validaciones (como por ejemplo, longitudes mínimas de 20 carácteres o máximas de 50),
        definir "estrategias" para autogenerar la llave primaria, definir el nombre de la columna (parecido al
        caso de las tablas arriba, con @Column(name = "nuevo nombre") ), entre muchos otros
        
        Para más información buscar "Hibernate Annotations"
    */

    //NOTA: los atributos DEBEN SER PRIVADOS y se deben establecer Getters y Setters. En nuestro caso esto
    //      lo hace Lombok gracias a la anotación @Data
    @Id
    private String email;
    private String username;
    private String password;
    private Boolean enabled;
}
