package pe.edu.upc.agroleak.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI agroLeakOpenApi() {
        return new OpenAPI().components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerAuth",
                        new io.swagger.v3.oas.models.security.SecurityScheme().type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth")).info(new Info()
                        .title("AgroLeak API")
                        .version("0.1.0")
                        .description("API REST del MVP académico de AgroLeak para monitoreo IoT de riego, alertas, control de válvula y observaciones de plagas.")
                        .contact(new Contact().name("AgroLeak Team - UPC")));
    }
}
