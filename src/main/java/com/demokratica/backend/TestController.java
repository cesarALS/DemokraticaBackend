package com.demokratica.backend;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/*
    Un RestController en Spring es una clase que recibe peticiones HTTP y las responde.
    La razón por la que lo llamé TestController es porque quería probar que mi configuración
    de seguridad sí me estuviera permitiendo acceder a las páginas informativas y a la landing
    page pero estuviera bloqueando todo lo demás. Por defecto Spring Security bloquea todo menos
    un Login que ellos mismos definen, así que tenía que probar que pudiera extender la cantidad
    de páginas que son accesibles y además redigir correctamente para conectar con el frontend
    
    Además de probar la configuración de seguridad también estoy probando que el endpoint que recibe
    usuario y contraseña funcione correctamente y guarde las contraseñas hasheadas en la base de datos

    TODO: Esta clase debería refactorizarla porque  está ejecutando lógica importante, no
    solo pruebitas bobas como antes, y además está mezclando funcionalidades que no tienen
    nada que ver como redireccionar a las páginas de demokrática pero también guardar usuarios
    mediante datos de formulario
 */
@RestController
public class TestController {
    
    //TODO: Crear un constructor que reciba estas dos variables como argumentos porque es una mejor
    //práctica que usar el Autowired aquí
    @Autowired
    private UsersRepository userRepository;
    @Autowired 
    private PasswordEncoder encoder;
    
    //TODO: un posible problema con el redireccionamiento es que si cambiamos las rutas en el frontend 
    //entonces tocará cambiarlas aquí también porque están hard-coded
    @GetMapping("/")
    public void getHomePage(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://demokratica.vercel.app/");
    }
    
    @GetMapping("/conozcanos")
    public void getConozcanosPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://demokratica.vercel.app/conozcanos");
    }

    @GetMapping("/ayuda")
    public void getAyudaPage(HttpServletResponse response) throws IOException{
        response.sendRedirect("http://demokratica.vercel.app/ayuda");
    }
    
    @GetMapping("/ingrese")
    public void getLoginPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://demokratica.vercel.app/ingrese");
    }

    /*
        Este es el endpoint que recibe datos de registro y los almacena en la base de datos. 
        Las peticiones son POST de la forma {url_demokratica}/user/?username="ejemplo"&password="ejemplo"
        El nombre de los parámetros debe ser ese y no se pueden enviar en formato JSON, toca en la URL
        Todo está hecho de forma muy manual para probar que funciona correctamente el hasheo y guardar
        en la BD. Una implementación más real debe usar el filtro de seguridad UsernamePasswordAuthenticationFilter,
        definir un AuthenticationManager (usualmente es del tipo ProviderManager), usar el 
        UsernamePasswordAuthenticationProvider o algo así y obtener un UsernamePasswordAuthenticationToken
      
        TODO: cambiar el parámetro username por un parámetro de correo. Esto también implica modificar
        la clase User.
        TODO: implementar la misma funcionalidad pero con las clases y métodos descritos arriba
     */
    @PostMapping("/users/")
    public ResponseEntity<User> signupUser(@RequestParam String username, 
                                        @RequestParam String password) {

        String hashedPassword = encoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        return new ResponseEntity<User>(savedUser, HttpStatus.CREATED);
    }
   
    /*
        El código está hecho a las patadas porque solo me interesaba probar que se estaba conectando
        correctamente con la BD y que yo estaba entendiendo correctamente cómo se debía llamar cada cosa. 
        Usualmentee esta lógica se implementaría en una clase aparte llamada UserService para que no haya
        tanto problema con el manejo de excepciones y tener que chequear si sí se encontró un usuario con ese
        ID
     */
    @GetMapping("/users/")
    public ResponseEntity<User> getUserById(@RequestParam String username) {
        User foundUser = new User();
        Boolean found = false;
        try {
            foundUser = userRepository.findById(username).orElseThrow(() -> 
                    new RuntimeException("Couldn't find a user with that name")
                );
            found = true;
        } catch (RuntimeException e) {
            System.out.println("No se pudo encontrar al usuario " + username);
        }

        if (!found) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<User>(foundUser, HttpStatus.OK);
    }
    
    
}
