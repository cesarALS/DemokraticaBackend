package com.demokratica.backend;

import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class TestController {
    
    @GetMapping("/")
    public void getHomePage(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://demokratica.vercel.app/");
    }
    
    @GetMapping("/conozcanos")
    public void getConozcanosPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("demokratica.vercel.app/conozcanos");
    }

    @GetMapping("/ayuda")
    public void getAyudaPage(HttpServletResponse response) throws IOException{
        response.sendRedirect("demokratica.vercel.app/ayuda");
    }
    
    @GetMapping("/ingrese")
    public void getLoginPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("demokratica.vercel.app/ingrese");
    }
    
}
