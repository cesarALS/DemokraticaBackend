package com.demokratica.backend;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.demokratica.backend.Repositories.UsersRepository;

@SpringBootTest
class BackendApplicationTests {

	//Solo estamos mirando que los tests puedan cargar el ApplicationContext
	//Si esto se da entonces userRepository debería ser no nulo
	@Autowired
	private UsersRepository userRepository;
	@Test
	void contextLoads() {
		Assertions.assertThat(userRepository).isNotNull();
	}

}
