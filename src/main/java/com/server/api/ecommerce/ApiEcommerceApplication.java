package com.server.api.ecommerce;

import java.util.List;

import com.server.api.ecommerce.config.AppConstants;
import com.server.api.ecommerce.entity.Role;
import com.server.api.ecommerce.repository.RoleRepository;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@SecurityScheme(name = "E-Commerce Application", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class ApiEcommerceApplication implements CommandLineRunner {

	private final RoleRepository roleRepository;

    public ApiEcommerceApplication(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
	 * Main method to run the Spring Boot application.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiEcommerceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			Role adminRole = new Role();
			adminRole.setId(AppConstants.ADMIN_ID);
			adminRole.setName("ADMIN");

			Role userRole = new Role();
			userRole.setId(AppConstants.USER_ID);
			userRole.setName("USER");

			List<Role> roles = List.of(adminRole, userRole);
			List<Role> savedRoles = roleRepository.saveAll(roles);
			savedRoles.forEach(System.out::println);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
