package com.milk_ecommerce_backend.security;

import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/users/login")
                || path.startsWith("/api/users/register")
                || path.startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();

        if (jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email;

        try {
            email = jwtUtil.extractEmail(jwt);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid token\"}");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User userEntity = userRepository.findByEmail(email).orElse(null);

            if (userEntity != null && jwtUtil.validateToken(jwt, email)) {

                String roleName = null;

                if (userEntity.getRole() != null) {
                    roleName = userEntity.getRole().getRoleName();
                }

                if (roleName == null || roleName.isBlank()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String normalizedRole = roleName.startsWith("ROLE_")
                        ? roleName.trim().toUpperCase()
                        : "ROLE_" + roleName.trim().toUpperCase();

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(normalizedRole);

                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        userEntity.getEmail(),
                        userEntity.getPassword(),
                        Collections.singletonList(authority)
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}