package jv.supermarket.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jv.supermarket.security.CustomUserDetailsService;
import jv.supermarket.security.filters.FilterTokenJWT;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    final FilterTokenJWT filterToken;
    
    WebSecurityConfig(FilterTokenJWT filterToken) {
        this.filterToken = filterToken;
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authManager(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConf() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200"); // Domínio do frontend
        config.addAllowedHeader("*");
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Aplica CORS em todas as rotas
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomUserDetailsService userDetailsService) throws Exception {
        String url_auth = "/supermarket/auth/";
        String url_products = "/supermarket/product/";
        String url_stocks = "/supermarket/stock/";
        String url_categories = "/supermarket/category/";
        String url_images = "/supermarket/image/";
        String url_carts = "/supermarket/cart/";
        String url_orders = "/supermarket/order/";
        String url_admin = "/supermarket/admin/";

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //@formatter:off
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/h2-console/**")
                    .permitAll()
                .requestMatchers(url_auth+"**")
                    .permitAll()
                .requestMatchers(
                        "/v3/api-docs/**",   // Documentação JSON
                        "/swagger-ui/**",    // Recursos Swagger UI
                        "/swagger-ui.html",  // Página inicial do Swagger
                        "/webjars/**"        // Recursos estáticos
                ).permitAll()
                
                .requestMatchers(HttpMethod.GET, url_products+"**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                .requestMatchers(HttpMethod.POST, url_products + "save")
                    .hasRole("ADMIN")
                    
                    
                .requestMatchers(HttpMethod.PUT, url_products + "**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, url_products + "**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, url_products+"{id:\\d+}/**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, url_stocks+"**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                .requestMatchers(HttpMethod.PUT, url_stocks+"{id:\\d+}/**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO")

                
                .requestMatchers(HttpMethod.POST, url_categories + "save")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, url_categories +"{id:\\d+}")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, url_categories +"{id:\\d+}")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, url_categories +"**")
                    .hasAnyRole("ADMIN", "CLIENTE", "FUNCIONARIO")
                    
                .requestMatchers(HttpMethod.POST, url_images+"upload")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, url_images +"{id:\\d+}")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, url_images +"{id:\\d+}")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, url_images+"**")
                    .hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                    
                .requestMatchers(HttpMethod.GET, url_carts+"show")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, url_carts+"addItem/{itemId:\\d+}")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.DELETE, url_carts+"removeItem/{itemId:\\d+}")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.PUT, url_carts+"item/{itemId:\\d+}/update")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.DELETE, url_carts+"clear")
                    .hasRole("CLIENTE")
                    
                .requestMatchers(HttpMethod.POST, url_orders+"create")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, url_orders+"{id:\\d+}")
                    .hasAnyRole("ADMIN","CLIENTE")
                .requestMatchers(HttpMethod.GET, url_orders+"by-user")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.DELETE, url_orders+"{id:\\d+}/cancel")
                    .hasRole("CLIENTE")
                    
                .requestMatchers(HttpMethod.POST, url_admin+"createEmployee")
                    .hasRole("ADMIN"));
            //@formatter:on

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filterToken, UsernamePasswordAuthenticationFilter.class);

        http.headers(header -> header
                .frameOptions(Customizer.withDefaults()).disable());

        http.csrf(csrf -> csrf.disable());

        http.cors(cors -> cors.configurationSource(corsConf()));

        http.userDetailsService(userDetailsService);

        return http.build();
    }

}
