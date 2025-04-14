package com.movio.moviolab.controllers;

import com.movio.moviolab.services.AsyncLogService;
import com.movio.moviolab.services.AsyncLogService.LogFileResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/logs")
@Tag(name = "Log Controller Async", description = "Позволяет работать с логами асинхронно")
public class LogControllerAsync {

    private final AsyncLogService asyncLogService;

    public LogControllerAsync(AsyncLogService asyncLogService) {
        this.asyncLogService = asyncLogService;
    }

    @Operation(summary = "Создание лог-файла",
            description = "Запускает асинхронный процесс создания лог-файла")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Процесс запущен"),
    })
    @PostMapping("/async")
    public ResponseEntity<Map<String, String>> requestLogsAsync(
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format yyyy-MM-dd")
            String date) {
        String taskId = asyncLogService.createLogTask(date);
        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }

    @Operation(summary = "Получение состояния",
            description = "Выводит состояние текущего процесса,"
                   + "если он завершился, выводит время до его удаления")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Вывод состояния процесса"),
        @ApiResponse(responseCode = "404", description = "Процесс не найден"),
    })
    @GetMapping("/async/status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        try {
            return ResponseEntity.ok(asyncLogService.getTaskStatus(taskId));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получение лог-файла",
            description = "Возвращает лог-файл")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Процесс запущен"),
        @ApiResponse(responseCode = "204", description = "Логи за предложенную дату не найдены"),
        @ApiResponse(responseCode = "404", description = "Такого процесса не существует"),
    })
    @GetMapping("/async/file/{taskId}")
    public ResponseEntity<ByteArrayResource> getLogFile(@PathVariable String taskId) {
        try {
            LogFileResult result = asyncLogService.getLogFile(taskId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + result.getFilename())
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(result.getContentLength())
                    .body(result.getResource());
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}
