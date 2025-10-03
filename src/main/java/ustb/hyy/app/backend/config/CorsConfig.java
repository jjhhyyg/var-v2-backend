package ustb.hyy.app.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * CORS跨域配置
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private Boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // 允许的HTTP方法
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // 允许的请求头
        if ("*".equals(allowedHeaders)) {
            config.setAllowedHeaders(Collections.singletonList("*"));
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // 允许携带凭证
        config.setAllowCredentials(allowCredentials);

        // 暴露的响应头
        config.addExposedHeader("Content-Disposition");

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
