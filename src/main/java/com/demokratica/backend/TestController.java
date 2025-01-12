package com.demokratica.backend;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

/*
    Un RestController en Spring es una clase que recibe peticiones HTTP y las responde.
    La razón por la que lo llamé TestController es porque quería probar que mi configuración
    de seguridad sí me estuviera permitiendo acceder a las páginas informativas y a la landing
    page pero estuviera bloqueando todo lo demás. Por defecto Spring Security bloquea todo menos
    un Login que ellos mismos definen, así que tenía que probar que pudiera extender la cantidad
    de páginas que son accesibles y además redigir correctamente para conectar con el frontend

    Actualmente esta clase sirve para verificar que el backend está corriendo y es reachable
 */
@RestController
public class TestController {
    /*
     * Todos estos métodos se podrían cambiar por uno solo con un @GetMapping(value = {url.mapping})
     * en el que el url.mapping es un argumento configurable desde la consola, por lo que bastaría con
     * reiniciar la aplicación con cierto comando para actualizar las rutas
     * Mirar esto:
     * https://stackoverflow.com/questions/29634724/how-to-do-multiple-url-mapping-aliases-in-spring-boot
     * 
     * Para implementar esa funcionalidad necesito extraer el value del GetMapping y append(earlo) a la
     * URL básica de demokratica
     */
    @GetMapping("/")
    public void getHomePage(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://demokratica.vercel.app/");
    }
    
    @GetMapping("/conozcanos")
    public void getConozcanosPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://demokratica.vercel.app/conozcanos");
    }

    @GetMapping("/precios")
    public void getPricesPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://demokratica.vercel.app/prices");
    }

    @GetMapping("/ayuda")
    public void getHelpPage(HttpServletResponse response) throws IOException{
        response.sendRedirect("https://demokratica.vercel.app/ayuda");
    }
    
    @GetMapping("/ingrese")
    public void getLoginPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://demokratica.vercel.app/ingrese");
    }

    @GetMapping("/unase")
    public void getSignupPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://demokratica.vercel.app/unase");
    }   
}