package com.demokratica.backend;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
/*
	Esta es la clase en la que se configura la seguridad de la aplicación: cuáles páginas requieren
	autenticación y cuáles no, cuáles requieren ciertos roles para ser accedidas, qué métodos de
	autenticación se van a usar (OAuth 2.0, login mediante formulario, login mediante headers o
	parámetros en la petición HTTP (llamado Http basic authentication)), si se va a usar protección
	contra un exploit llamado CSRF, entre otros

	Otro tipo de "configuración" que realizamos es definir unos métodos que retornan objetos de diversos
	tipos relacionados con seguridad.
	Mediante la anotación @Bean le decimos a Spring que si la aplicación en algún momento llega a necesitar
	un objeto del tipo que retorna ese método, que llame al método para obtenerlo.
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
		Los objetos de tipo UserDetailsManager son fundamentales en Spring Security. Son los que se
		encargan de almacenar los datos de los usuarios (incluyendo la contraseña y sus roles) y también
		de gestionarlos (para cambiar la contraseña por ejemplo). En el fondo Spring Security los 
		usa en el proceso de autenticación mediante usuario y contraseña.
		Spring Security necesita que le definamos cuál de todas las implementaciones queremos y cómo
		está configurada, porque hay muchas posibles implementaciones y configuraciones: para autenticación 
		con servidor LDAP, autenticación en memoria (una mierda que solo usan en demos triviales que no 
		enseñan nada) o autenticación con una base de datos, todas configurables.
		Al marcar este método con @Bean y retornar un UserDetailsManager le indicamos a Spring que si 
		necesita un UserDetailsManager lo obtenga llamando a este método

		JdbcUserDetailsManager es la implementación usada para acceder a bases de datos de verdad (no esa
		mierda de InMemoryUserDetailsManager, que es de juguete)
		El DataSource se obtiene por inyección de dependencias. Spring lee el application.properties, que
		es donde tenemos toda la información necesaria para las conexiones a la base de datos, y con eso
		él mismo crea el DataSource y nos lo pasa para que podamos construir el JdbcUserDetailsManager
	 */
	@Bean
	public UserDetailsService getJdbc(DataSource dataSource) {
		JdbcUserDetailsManager jdbc = new JdbcUserDetailsManager(dataSource);
		jdbc.setUsersByUsernameQuery("SELECT email,password,enabled FROM users WHERE email = ?");
		jdbc.setAuthoritiesByUsernameQuery("SELECT email,authority FROM authorities WHERE email = ?");
		return jdbc;
	}

	/*
	 * Al marcar este método con la anotación @Bean y retornar un objeto de tipo AuthenticationManager le
	 * estamos diciendo a Spring que si necesita un AuthenticationManager lo consiga llamando a este método,
	 * en el que configuramos el nuestro
	 * 
	 * Le estamos pidiendo que nos inyecte un UserDetailsService para poder inicializar el AuthenticationManager
	 * El que nos va a inyectar es el JdbcUserDetailsManager, que es una "subclase" de UserDetailsService así
	 * que tiene un tipo compatible
	 * (Observación: también le estamos pidiendo que nos inyecte un PasswordEncoder. Si el método no lo
	 * llamamos nosotros e incluye objetos que no son atributos de esta clase, significa que los estamos
	 * obteniendo mediante inyección de dependecias)
	 */
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder encoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(encoder);
		authenticationProvider.setUserDetailsService(userDetailsService);

		return new ProviderManager(authenticationProvider);
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