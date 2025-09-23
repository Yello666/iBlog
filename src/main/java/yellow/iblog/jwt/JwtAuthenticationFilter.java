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
            if (JwtUtils.validateToken(token)) {
                Claims claims = JwtUtils.parseToken(token);
                String username=(String)claims.get("username");
                String role=(String) claims.get("role");
                // uid从subject获取：
                String uidStr = claims.getSubject();
//                Long uid = Long.valueOf(uidStr);  // 将字符串转回Long,没必要，getSubject默认返回的就是String
                request.setAttribute("uid", uidStr);//这里uid是当作subject来存的， 没有存到claims里面
                //这里uid就是long来存的
                request.setAttribute("username", claims.get("username"));
                request.setAttribute("role",claims.get("role"));
                // 构造 Spring Security 的认证对象
                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(role));

                //这里的第一个参数是subject，到时候getName()返回的值
                //第二个参数是认证时的密码或 token，一般在登录阶段用来比对密码，但是在其它函数那里比对，所以这里不填
                //第三个参数是角色 "ROLE_USER"...
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uidStr, null, authorities);

                // 存入 Spring Security 的上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                //token不对或者没有token
                ApiResponse<Object> apiResponse = ApiResponse.fail(401,"您没有足够的权限或者登陆已经过期，请联系工作人员");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
                return;

            }
        }

        filterChain.doFilter(request, response);
    }
}
