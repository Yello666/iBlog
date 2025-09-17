package yellow.iblog;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

//使用mybatis-plus启动类上要扫描mapper，不然无法运行
@SpringBootApplication
@MapperScan("yellow.iblog.mapper") //括号里面的是Mapper的包的地址
@Slf4j
public class IBlogApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context =SpringApplication.run(IBlogApplication.class, args);
        // 获取环境变量
        Environment env = context.getEnvironment();
        // 获取端口号
        String port = env.getProperty("server.port");
        // 获取主机地址
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        log.info("系统已经启动，访问地址http://{}:{}",hostAddress,port);
    }

}

