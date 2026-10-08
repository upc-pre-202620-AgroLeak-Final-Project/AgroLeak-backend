package pe.edu.upc.agroleak.common.config;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import java.nio.charset.StandardCharsets;
@Configuration
@Profile("prod")
public class ProductionConfig {
    public ProductionConfig(Environment env) {
        String secret=required(env,"agroleak.jwt.secret");
        if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT_SECRET requiere al menos 32 bytes en PROD");
        String origins=required(env,"agroleak.cors.allowed-origins");
        if(origins.contains("*")) throw new IllegalArgumentException("CORS_ALLOWED_ORIGINS debe enumerar origenes en PROD");
        for(String origin:origins.split(",")) {
            var uri=java.net.URI.create(origin.strip());
            if(!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost()==null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null || !uri.getPath().isEmpty())
                throw new IllegalArgumentException("Origen CORS invalido");
        }
        if(!required(env,"spring.datasource.url").startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("DB_URL debe usar jdbc:postgresql://");
        required(env,"spring.datasource.username"); required(env,"spring.datasource.password");
    }
    private String required(Environment env,String name) {
        String value=env.getRequiredProperty(name);
        if(value.isBlank() || value.contains("${")) throw new IllegalArgumentException("Configuracion requerida: " + name);
        return value;
    }
}
