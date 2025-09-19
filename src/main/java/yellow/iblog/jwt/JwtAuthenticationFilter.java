package yellow.iblog.jwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import yellow.iblog.Common.ApiResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component //可以装配
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.parseToken(token);
                String username=claims.getSubject();
                String role=(String) claims.get("role");

                // 这里你可以把用户信息放到 request
                request.setAttribute("uid", claims.get("uid"));
                request.setAttribute("username", claims.getSubject());
                request.setAttribute("role",claims.get("role"));
                // 构造 Spring Security 的认证对象
                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // 存入 Spring Security 的上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                //token不对或者没有token
                ApiResponse<Object> apiResponse = ApiResponse.fail("您没有足够的权限或者登陆已经过期，请联系工作人员");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
                return;

            }
        }

        filterChain.doFilter(request, response);
    }
}
