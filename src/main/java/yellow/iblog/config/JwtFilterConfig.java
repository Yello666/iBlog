package yellow.iblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import yellow.iblog.jwt.JwtAuthenticationFilter;
@Configuration
@Slf4j
public class JwtFilterConfig {
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter());
//           /user/* → 只能匹配 /user/xxx，不能匹配 /user
//          /user/** → 可以匹配 /user、/user/xxx、/user/xxx/yyy，包含user的全都匹配
        //只要访问以下的路由，springboot就会查看你有没有携带合法的jwt
        registration.addUrlPatterns("/user/**"); // 保护的接口路径
        registration.addUrlPatterns("/article/**"); // 保护的接口路径
        registration.addUrlPatterns("/comments/**"); // 保护的接口路径
        return registration;
    }
}

