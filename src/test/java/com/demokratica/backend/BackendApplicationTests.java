package com.demokratica.backend;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.Services.SessionService;

@SpringBootTest
class BackendApplicationTests {

	//Solo estamos mirando que los tests puedan cargar el ApplicationContext
	//Si esto se da entonces userRepository debería ser no nulo
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SessionsRepository sessionsRepository;
	@Autowired
	private UsersRepository usersRepository;

	@Test
	void sessionServiceLoads() {
		Assertions.assertThat(sessionService).isNotNull();
	}

	@Test
	void sessionsRepositoryLoads() {
		Assertions.assertThat(sessionsRepository).isNotNull();
	}

	@Test
	void usersRepositoryLoads() {
		Assertions.assertThat(usersRepository).isNotNull();
	}

}