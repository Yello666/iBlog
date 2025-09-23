package yellow.iblog.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


public class JwtUtils {
    // 生成一个秘钥（也可以配置在 application.yml）
    //这里是随机生成一个密钥
//    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final String SECRET = "emily-is-gonna-be-rich-888888888888888888"; // 必须至少32字节（32个字符）
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION = 1000 * 60 * 60*24; // 1天（以ms为单位）

    // 生成 token
    public static String generateToken(Long uid, String username,String role) {
        return Jwts.builder()
                .setSubject(uid.toString())//注意存储的是字符串//比对的时候还是用Long
                .claim("role", role)//添加构成jwt的参数
                .claim("username",username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // 解析 token
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder() // 创建 JWT 解析器建造器
                .setSigningKey(key) // 验证签名密钥，如果失败会抛出异常
                .build() // 构建解析器
                .parseClaimsJws(token) // 解析 Token
                .getBody(); // 获取 Token 的载荷 (Claims) 部分
    }

    // 校验 token 是否有效
    public static boolean validateToken(String token) {
        try {
            parseToken(token); // 尝试解析 Token，并且验证密钥
            return true; // 如果没有异常，说明 Token 有效
        } catch (JwtException e) {
            return false; // 如果捕获到异常，说明 Token 无效（签名错误或过期）
        }
    }
}
