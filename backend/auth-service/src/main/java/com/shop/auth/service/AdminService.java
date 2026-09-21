package com.shop.auth.service;

import com.shop.auth.dto.UserResponse;
import com.shop.auth.entity.User;
import com.shop.auth.exception.UserNotFoundException;
import com.shop.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для администрирования пользователей (доступ только у роли ADMIN).
 * <p>
 * Содержит бизнес-логику управления пользователями:
 * - получение списка всех пользователей
 * - получение пользователя по id
 */
// @Transactional - данную аннотацию будем использовать непосредственно на каждом методе, чтобы указать явно будет ли транзакцияд readonly или нет
@Service
public class AdminService {
    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param userRepository репозиторий для работы с пользователями
     */
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Поиск пользователя по id
     *
     * @param userId id пользователя
     * @return DTO UserResponse
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapToUserResponse(user);
    }

    /**
     * Поиск всех пользователей
     *
     * @return Последовательность DTO UserResponse
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    /**
     * Преобразует сущность User в UserResponse DTO.
     *
     * @param user сущность пользователя
     * @return DTO для ответа клиенту
     */
    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        return response;
    }
}