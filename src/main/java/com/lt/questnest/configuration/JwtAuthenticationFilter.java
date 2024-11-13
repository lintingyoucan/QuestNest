package com.lt.questnest.configuration;

import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityConfig securityConfig;

    @Autowired
    public JwtAuthenticationFilter(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("取出的path路径:{}", path);

        // 检查请求路径是否在允许的路径中
        if (path.equals("/loginByPasswd") || path.equals("/register")
                || path.equals("/sendCode") || path.equals("/loginByCode")
                || path.equals("/resetPasswd") || path.equals("/images")
        || path.equals("/admin/login") || path.equals("/chat")) {
            // 直接放行
            chain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // 直接放行
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");
        logger.info("从请求头截取的原始Token:{}", token);
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            logger.info("截取出来的jwtToken：{}", jwtToken);
            if (securityConfig != null) {
                if (securityConfig.validateToken(jwtToken)) {
                    // 进行后续处理
                    logger.info("配置类中返回验证Token结果为True，进入后续处理");

                    String email = Jwts.parser().
                            setSigningKey(securityConfig.getSecret()).
                            parseClaimsJws(jwtToken).
                            getBody().
                            getSubject();
                    logger.info("从token中取出的email:{}", email);

                    String role = (String) Jwts.parser().
                            setSigningKey(securityConfig.getSecret()).
                            parseClaimsJws(jwtToken).
                            getBody().
                            get("role");
                    logger.info("从token中取出的role:{}", role);

                    // 将角色信息转换为GrantedAuthority
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority(role.trim()));
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    chain.doFilter(request, response);
                } else {
                    // 处理 token 无效的情况
                    logger.warn("Invalid token无效token");
                }
            } else {
                logger.error("SecurityConfig is null为空");
            }
        } else {
            logger.warn("Authorization header is missing or malformed请求头丢失");
        }


    }
}


