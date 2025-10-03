package ustb.hyy.app.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VAR熔池视频分析系统 API")
                        .version("1.0.0")
                        .description("""
                                VAR（真空自耗电弧重熔）熔池视频分析系统后端API文档。

                                主要功能：
                                - 视频上传与任务管理
                                - 实时进度查询
                                - 分析结果获取（动态参数、异常事件、追踪物体）
                                - AI模块回调接口
                                """)
                        .contact(new Contact()
                                .name("侯阳洋")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("生产环境")
                ));
    }
}
