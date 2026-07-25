package com.tgs.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * {@code pageSerializationMode = VIA_DTO} serializa las páginas
 * ({@link org.springframework.data.domain.Page}) usando un DTO estable
 * (PagedModel) en vez de la forma interna de {@code PageImpl}. Esto
 * evita el warning de Spring y garantiza que el contrato JSON no cambie
 * entre versiones.
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class EcommerceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApiApplication.class, args);
	}

}
