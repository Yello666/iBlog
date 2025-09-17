package yellow.iblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Slf4j
public class security {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 csrf，否则 POST 请求可能会报 403
                .csrf(csrf -> csrf.disable())
                // 放行所有请求，不需要登录认证
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        log.info("安全插件已经加载");
        return http.build();
    }
}
