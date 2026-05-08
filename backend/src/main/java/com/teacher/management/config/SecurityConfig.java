package com.teacher.management.config;

import com.teacher.management.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security + JWT 配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行登录和注册接口
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chat").permitAll()
                        // 管理员专用接口需要 ADMIN 角色
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 部门主任专用接口需要 DEPT_DIRECTOR 角色
                        .requestMatchers("/api/dept-director/**").hasRole("DEPT_DIRECTOR")
                        // 教师专用接口需要 TEACHER 角色（部门主任也拥有此权限）
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")

                        // 用户管理（增删改 + 列表查询 + 详情查询）
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/list").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}/detail").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}/detail").hasRole("ADMIN")
                        // 院系管理（保留 GET /api/departments/all 对所有登录用户开放，教师端下拉需要）
                        .requestMatchers(HttpMethod.POST, "/api/departments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/departments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/departments/list").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/departments/{id}/members").hasRole("ADMIN")
                        // 公告管理（增删改 + 列表查询）
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notices/list").hasRole("ADMIN")
                        // 系统配置（读 + 写）
                        .requestMatchers(HttpMethod.PUT, "/api/config/batch").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/config/list").hasRole("ADMIN")

                        // 其他所有 /api/** 接口需要认证（个人资料、密码修改、公告查看等）
                        .requestMatchers("/api/**").authenticated()
                        // 非 API 请求放行（静态资源等）
                        .anyRequest().permitAll())
                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
