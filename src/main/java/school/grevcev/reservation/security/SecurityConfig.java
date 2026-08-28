package school.grevcev.reservation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Для REST API с JWT это не нужно (stateless, нет кук)
                .csrf(csrf -> csrf.disable())

                // Без сессий — каждый запрос аутентифицируется токеном
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Правила доступа
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (регистрация/логин)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        // Swagger в dev (чтобы UI работал без токена)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // H2-консоль в dev (если используешь)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Всё остальное под /api — требует аутентификации
                        .requestMatchers("/api/**").authenticated()
                        // Прочее — закрываем
                        .anyRequest().denyAll()
                )

                // Вставляем наш JWT-фильтр ПЕРЕД стандартным UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
