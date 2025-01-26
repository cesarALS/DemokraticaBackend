package com.demokratica.backend.Configurations;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		/*
			TODO: activar la protección contra CSRF y enviar tokens CSRF con cada petición
			TODO: establecer una configuración completa y funcional y eliminar el resto de maricadas
		 */
			.csrf(csrf -> csrf.disable()) 
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/ingrese", "/unase", "/token-info", "/validar_token").permitAll()
				.anyRequest().authenticated()
			);

		return http.build();
	}

	@Bean
	public UserDetailsService getJdbc(DataSource dataSource) {
		JdbcUserDetailsManager jdbc = new JdbcUserDetailsManager(dataSource);
		jdbc.setUsersByUsernameQuery("SELECT email,password,enabled FROM users WHERE email = ?");
		jdbc.setAuthoritiesByUsernameQuery("SELECT email,authority FROM authorities WHERE email = ?");
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
}