package com.aguardi.ecommerce;

import org.springframework.boot.SpringApplication;

public class TestAguardiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AguardiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
