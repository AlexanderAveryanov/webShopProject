package com.shop.auth.security;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.entity.User;
import com.shop.auth.exception.UserNotFoundException;
import com.shop.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Фильтр для аутентификации запросов по JWT-токену.
 * <p>
 * Перехватывает каждый запрос, проверяет наличие и валидность JWT, устанавливает аутентификацию в SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * Извлекает JWT-токен из заголовка Authorization.
     * <p>
     * Ожидается формат: "Bearer <token>"
     *
     * @param request HTTP-запрос
     * @return токен или null, если заголовка нет или он не в формате Bearer
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(AuthResponse.BEARER + " ")) {
            return bearerToken.substring(AuthResponse.BEARER.length() + 1);
        }
        return null;
    }

    /**
     * Основной метод фильтра, вызываемый для каждого HTTP-запроса
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров для продолжения обработки запроса
     * @throws ServletException если произошла ошибка сервлета
     * @throws IOException      если произошла ошибка ввода/вывода
     *
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if(token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            // Создаем объект аутентификации
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, // principal — объект пользователя
                    null, // credentials — пароль не нужен, т.к. уже проверен
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())) // authorities — права
            );
            // Устанавливаем аутентификацию в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}
