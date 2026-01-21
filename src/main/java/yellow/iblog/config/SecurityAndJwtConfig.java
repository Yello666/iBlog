package yellow.iblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import yellow.iblog.jwt.JwtAuthenticationFilter;

import java.util.Arrays;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()   // 登录/注册接口放行
                         .requestMatchers("/article/**").permitAll()
                        .requestMatchers("/comments/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 只有管理员能访问
                        .requestMatchers("/user/**").permitAll()
                        .requestMatchers("/tags/**").permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS配置源
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（前端地址）
        configuration.setAllowedOrigins(Arrays.asList(
            "https://www.yellow-iblog.cn",  // 生产环境前端域名
             "https://yellow-iblog.cn",
            "http://localhost:5173",        // 本地开发环境（Vite）
            "http://127.0.0.1:5173"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        
        // 允许发送凭证（cookies、authorization headers等）
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 允许的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径应用CORS配置
        
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}