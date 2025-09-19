package yellow.iblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import yellow.iblog.jwt.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // 启用@PreAuthorize等注解
public class SecurityAndJwtConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityAndJwtConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()   // 登录/注册接口放行
                         .requestMatchers("/article/**").permitAll()
                        .requestMatchers("/comments/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 只有管理员能访问
                        .requestMatchers("/user/**").authenticated()
//                        .anyRequest().authenticated()              // 其他都要认证，登陆了就可以访问
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
