package com.movio.moviolab.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@Tag(name = "Visit Stats Controller", description = "Контроллер для ведения статистики посещения.")
public class VisitStatsController {
    private final VisitCounterService service;

    public VisitStatsController(VisitCounterService service) {
        this.service = service;
    }

    @Operation(summary = "Добавление посещения URL вручную",
            description = "Добавляет в счётчик одно посещение указанного URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Посещение добавлено"),
    })
    @PostMapping("/record")
    public void recordVisit(@RequestParam String url) {
        service.recordVisit(url);
    }

    @Operation(summary = "Количество обращений к URL",
            description = "Возвращает количество обращений к конкретному URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Количество обращений возвращено"),
    })
    @GetMapping("/count")
    public long getVisitCount(@RequestParam String url) {
        return service.getVisitCount(url);
    }

    @Operation(summary = "Количество общее обращений",
            description = "Возвращает количество обращений по всем URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Количество обращений возвращено"),
    })
    @GetMapping("/all")
    public Map<String, Long> getAllStats() {
        return service.getAllStats();
    }
}
