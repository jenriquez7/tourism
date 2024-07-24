package com.tourism.configuration.interceptor;

import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.configuration.annotation.RequiresRoles;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Value("${session.client-secret}")
    private String secretKey;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthorizationInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();

            RequiresRoles annotation = method.getAnnotation(RequiresRoles.class);
            if (annotation != null) {
                Set<Role> requiredRoles = new HashSet<>(Arrays.asList(annotation.value()));
                String token = extractJwtFromRequest(request);

                if (token == null || !jwtTokenProvider.validateAccessToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }

                if (!userHasRequiredRole(requiredRoles, token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }

        return true;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean userHasRequiredRole(Set<Role> requiredRoles, String token) {
        Set<Role> userRoles = decodeRolesFromJwt(token);
        return userRoles.stream().anyMatch(requiredRoles::contains) || requiredRoles.stream().anyMatch(Role.EVERY_ROL::equals);
    }

    private Set<Role> decodeRolesFromJwt(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        Claims claims = claimsJws.getPayload();
        Object rolesObj = claims.get("role");
        Set<Role> roles = new HashSet<>();

        if (rolesObj instanceof String rol) {
            roles.add(Role.valueOf(rol));
        }

        return roles;
    }

}
