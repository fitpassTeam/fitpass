package org.example.fitpass.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
            .title("FitPassAPI Document")
            .version("v0.0.1")
            .description("FitPass Swagger API 명세서");
        return new OpenAPI()
            .components(new Components())
            .info(info);
    }

    @Bean
    public ParameterCustomizer parameterCustomizer() {
        return (parameter, methodParameter) -> {
            if (methodParameter.getParameterType().equals(Pageable.class)) {
                // Pageable 파라미터의 sort 필드에 대한 기본값 설정
                if (parameter.getName().equals("sort")) {
                    return parameter
                        .schema(new StringSchema()
                            ._default("id,desc")
                            .example("id,desc")
                            .description("정렬 조건 (필드명,방향). 예: id,desc 또는 name,asc"))
                        .required(false);
                }
                // page 파라미터
                if (parameter.getName().equals("page")) {
                    return parameter
                        .schema(new IntegerSchema()
                            ._default(0)
                            .example(0)
                            .description("페이지 번호 (0부터 시작)"))
                        .required(false);
                }
                // size 파라미터
                if (parameter.getName().equals("size")) {
                    return parameter
                        .schema(new IntegerSchema()
                            ._default(10)
                            .example(10)
                            .description("페이지 크기"))
                        .required(false);
                }
            }
            return parameter;
        };
    }
}

