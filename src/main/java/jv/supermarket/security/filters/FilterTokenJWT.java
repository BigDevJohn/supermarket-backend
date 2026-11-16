package jv.supermarket.security.filters;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jv.supermarket.auth.TokenService;
import jv.supermarket.shared.customexception.ResourceNotFoundException;
import jv.supermarket.user.Role;
import jv.supermarket.user.User;
import jv.supermarket.user.UserRepository;

@Component
public class FilterTokenJWT extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    FilterTokenJWT(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = this.recoverToken(request);
        if (token != null) {
            String login = tokenService.validateToken(token);

            if (login != null) {
                User user = userRepository.findByEmail(login);

                if (user == null) {
                    throw new ResourceNotFoundException("User not found with email: " + login);
                }
                Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toSet());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null,
                        authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String basePath = "/supermarket/auth";
        return path.equals(basePath + "/login") || path.equals(basePath + "/registrar");
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "").trim();
    }
}
