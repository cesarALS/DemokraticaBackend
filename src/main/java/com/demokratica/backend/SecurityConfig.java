package com.demokratica.backend;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/*
	Esta es la clase en la que se configura la seguridad de la aplicación: cuáles páginas requieren
	autenticación y cuáles no, cuáles requieren ciertos roles para ser accedidas, qué métodos de
	autenticación se van a usar (OAuth 2.0, login mediante formulario, login mediante headers o
	parámetros en la petición HTTP (llamado Http basic authentication)), si se va a usar protección
	contra un exploit llamado CSRF, entre otros
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/*
		El concepto fundamental de Spring Security es la SecurityFilterChain, una cadena de filtros de
		seguridad que en cada filtro realiza alguna operación como tratar de leer credenciales de acceso y
		autenticar al usuario (hay filtros separados para Http Basic Authentication, Login Authentication,
		OAuth Authentication), leer el token de CSRF para evitar exploits de ese tipo, etc.
		
		Al estar anotado con @Bean y devolver un SecurityFilterChain le estamos diciendo a Spring que la
		SecurityFilterChain que queremos usar en la aplicación es la retornada por este método.
		Recibe un parámetro de tipo HttpSecurity. Spring nos va a inyectar esta dependencia, que es un
		builder (patrón de diseño) para crear SecurityFilterChains
		La sintaxis tan extraña proviene de que cada método de HttpSecurity configura una parte de la 
		seguridad y luego se retorna a sí mismo para así permitir el encadenamiento de las llamadas

		Las expresiones del tipo ( (csrf) -> csrf.disable()) son expresiones lambda. Básicamente son
		funciones sin nombre que reciben cierto número de parámetros y realizan alguna operación con ellos.
		Por ejemplo, el método http.csrf() espera como parámetro UNA FUNCIÓN que reciba como parámetro a un
		objeto de una clase específica que tiene métodos para configurar CSRF (literalmente CsrfConfigurer)
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		/*
			Desactive la protección contra exploits de tipo CSRF para facilitar el desarrollo.
			Esta protección evita que se pueda realizar POST requests sin enviar un token de CSRF junto con
			los datos del formulario.
			Como no sé crear ese tipo de tokens y me lo complica todo lo desactivé, aunque lo ideal sería
			que estuviera activado
			TODO: activar la protección contra CSRF y enviar tokens CSRF con cada petición
		 */
			.csrf(csrf -> csrf.disable()) 
			.authorizeHttpRequests((authorize) -> authorize
				//.requestMatchers("/", "/conozcanos", "/ayuda").permitAll()
				.anyRequest().permitAll()
			);
			/*
				Tengo un montón de métodos comentados para que me sea más fácil ir probando
				cambios sin tener que consultar cómo es la sintaxis de cada una de estas maricadas
				TODO: establecer una configuración completa y funcional y eliminar el resto de maricadas
			 */
			//.httpBasic(Customizer.withDefaults())
			// .formLogin(formLogin -> formLogin
			//	.loginPage("/ingrese")
			//	.permitAll());
			//.formLogin(Customizer.withDefaults());

		return http.build();
	}

	/*
		
	 */
	@Bean
	public UserDetailsManager users(DataSource dataSource) {
		JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
		return users;
	}

	/*
		Para hashear las contraseñas se puede usar una enorme diversidad de métodos y Spring no sabe
		cuál queremos.
		Por eso con este @Bean le decimos que si llegamos a necesitar un PasswordEncoder en la aplicación
		lo obtenga a partir de este método
		El DelegatingPasswordEncoder es el método de hasheo más recomendable. Por defecto cifra con un
		algoritmo muy seguro llamado bcrypt y es capaz de hashear contraseñas con todos los demás métodos en
		caso de ser necesario (por eso se llama Delegating, porque le delega a los otros si es necesario)
	 */
	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}