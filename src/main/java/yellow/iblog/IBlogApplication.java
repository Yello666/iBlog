package yellow.iblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//使用mybatis-plus启动类上要扫描mapper，不然无法运行
@SpringBootApplication
@MapperScan("yellow.iblog.mapper") //括号里面的是Mapper的包的地址
public class IBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(IBlogApplication.class, args);
    }

}
