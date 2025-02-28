package com.demokratica.backend.Security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.demokratica.backend.Exceptions.UnsupportedAuthenticationException;
import com.demokratica.backend.Services.JWTService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		http
		/*
			TODO: activar la protección contra CSRF y enviar tokens CSRF con cada petición
		 */
			.csrf(csrf -> csrf.disable()) 
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers(HttpMethod.DELETE, "/api/sessions/").permitAll()
				.requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()
				.requestMatchers("/api/webhooks/mercadopago").permitAll() // La validación con x-request se hará manualmente
				.anyRequest().authenticated() 
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public UserDetailsService getJdbc(DataSource dataSource) {
		JdbcUserDetailsManager jdbc = new JdbcUserDetailsManager(dataSource);
		jdbc.setUsersByUsernameQuery("SELECT email,password,enabled FROM users WHERE email = ?");
		jdbc.setAuthoritiesByUsernameQuery("SELECT user_email,authority FROM authorities WHERE user_email = ?");
		return jdbc;
	}

	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder encoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(encoder);
		authenticationProvider.setUserDetailsService(userDetailsService);

		return new ProviderManager(authenticationProvider);
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public JwtAuthenticationFilter getJwtFilter(JWTService jwtService, JwtAuthenticationProvider jwtAuthProvider) {
		return new JwtAuthenticationFilter(jwtService, jwtAuthProvider);
	}

	@Bean
	public JwtAuthenticationProvider getJwtAuthProvider() {
		return new JwtAuthenticationProvider();
	}

	public static String getUsernameFromAuthentication() throws UnsupportedAuthenticationException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = "";
		if (auth.getClass().getName().equals(UsernamePasswordAuthenticationToken.class.getName())) {
			email = (String) ((UserDetails) auth.getPrincipal()).getUsername();
		} else if (auth.getClass().getName().equals(JwtAuthentication.class.getName())) {
			email = (String) auth.getPrincipal();
		} else {
			throw new UnsupportedAuthenticationException(auth);
		}

		return email;
	}
}