package com.teacher.management.filter;

import com.teacher.management.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器
 * 从请求头中提取 Token，验证后将用户信息放入 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtUtils.validateToken(token)) {
                    Claims claims = jwtUtils.parseToken(token);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);

                    // 【B01/B03 防御】role 为空时直接跳过认证
                    if (role == null || role.isBlank()) {
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 创建权限列表
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                    // 部门主任同时拥有教师权限，使其可以访问教师端接口
                    if ("dept_director".equalsIgnoreCase(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
                    }

                    // 创建认证对象，放入 SecurityContext
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities);

                    // 将 JWT claims 中的用户信息存入 details，供 SecurityUtils 零查询访问
                    Map<String, Object> details = new HashMap<>();
                    details.put("userId", claims.get("userId", Long.class));
                    details.put("roleId", claims.get("roleId", Long.class));
                    Long deptId = claims.get("deptId", Long.class);
                    if (deptId != null) {
                        details.put("deptId", deptId);
                    }
                    authentication.setDetails(details);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token 无效，不设置认证信息，后续由 Security 拦截
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
