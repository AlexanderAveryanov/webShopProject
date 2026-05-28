package com.shop.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/** DTO для стандартизированного ответа при возникновении ошибок в REST API */
@Data // Генерирует: @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Builder // Реализует паттерн Builder для удобного создания объектов
@NoArgsConstructor // Генерирует конструктор без параметров
@AllArgsConstructor // Генерирует конструктор со всеми полями
public class ErrorResponse {
    private LocalDateTime timestamp;     // Время ошибки
    private int status;                  // HTTP статус код
    private String error;                // Наименование ошибки по статусу
    private String message;              // Сообщение
    private Map<String, String> details; // Детали ошибки (поле -> причина)
}