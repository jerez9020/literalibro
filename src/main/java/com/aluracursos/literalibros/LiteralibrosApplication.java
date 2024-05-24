package com.aluracursos.literalibros;

import com.aluracursos.literalibros.medel.Libro;
import com.aluracursos.literalibros.principal.Principal;
import com.aluracursos.literalibros.repository.LIbroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteralibrosApplication  implements CommandLineRunner {

	@Autowired
	private LIbroRepository repository;
	public static void main(String[] args) {
		SpringApplication.run(LiteralibrosApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(repository);
		principal.muestraElMenu();
	}
}
