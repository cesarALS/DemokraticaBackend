package com.demokratica.backend;

import org.springframework.data.jpa.repository.JpaRepository;

/*
    Los objetos de tipo JpaRepository implementan un montón de métodos para consultar a las bases
    de datos. Por ejemplo:
    save(), findById(), findAll(), count(), delete()
    Para más información mirar
    https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html

    Basta con extender la interfaz para poder llamar a todos estos métodos con sus implementaciones por defecto
    Solo es necesario que en la parte de JpaRepository< , > el primer valor sea la clase que corresponde a la
    tabla y el segundo sea el tipo de su ID 
 */
public interface UsersRepository extends JpaRepository<User, String>{
    
}
