package com.movio.moviolab.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Логирование входа в методы CommentController
    @Before("execution(* com.movio.moviolab.controllers.CommentController.*(..))")
    public void logBeforeCommentController(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Вход в метод CommentController: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    // Логирование выхода из методов CommentController
    @AfterReturning(pointcut = "execution(* com.movio.moviolab.controllers"
            + ".CommentController.*(..))", returning = "result")
    public void logAfterReturningCommentController(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Выход из метода CommentController: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    // Логирование ошибок в методах CommentController
    @AfterThrowing(pointcut = "execution(* com.movio.moviolab.controllers"
           + ".CommentController.*(..))", throwing = "error")
    public void logAfterThrowingCommentController(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в методе CommentController: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    // Аналогичные аспекты для UserController
    @Before("execution(* com.movio.moviolab.controllers.UserController.*(..))")
    public void logBeforeUserController(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Вход в метод UserController: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "execution(* com.movio.moviolab"
            + ".controllers.UserController.*(..))", returning = "result")
    public void logAfterReturningUserController(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Выход из метода UserController: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    @AfterThrowing(pointcut = "execution(* com.movio.moviolab"
            + ".controllers.UserController.*(..))", throwing = "error")
    public void logAfterThrowingUserController(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в методе UserController: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    // Логирование входа в методы MovieController
    @Before("execution(* com.movio.moviolab.controllers.MovieController.*(..))")
    public void logBeforeMovieController(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Вход в метод MovieController: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    // Логирование выхода из методов MovieController
    @AfterReturning(pointcut = "execution(* com.movio.moviolab"
            + ".controllers.MovieController.*(..))", returning = "result")
    public void logAfterReturningMovieController(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Выход из метода MovieController: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    // Логирование ошибок в методах MovieController
    @AfterThrowing(pointcut = "execution(* com.movio.moviolab"
            + ".controllers.MovieController.*(..))", throwing = "error")
    public void logAfterThrowingMovieController(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в методе MovieController: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    @AfterThrowing(pointcut = "execution(* com.movio.moviolab"
           + ".services.CommentService.*(..))", throwing = "error")
    public void logCommentServiceError(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в сервисе CommentService: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    // Логирование ошибок при работе с пользователями
    @AfterThrowing(pointcut = "execution(* com.movio.moviolab"
            + ".services.UserService.*(..))", throwing = "error")
    public void logUserServiceError(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в сервисе UserService: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    // Логирование ошибок при работе с фильмами
    @AfterThrowing(pointcut = "execution(* com.movio.moviolab"
            + ".services.MovieService.*(..))", throwing = "error")
    public void logMovieServiceError(JoinPoint joinPoint, Throwable error) {
        if (logger.isErrorEnabled()) {
            logger.error("Ошибка в сервисе MovieService: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }

    @Before("execution(* com.movio.moviolab.cache.*.*(..))")
    public void logCacheOperations(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Операция с кэшем: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "execution(* com.movio.moviolab.cache.InMemoryCache.put(..))")
    public void logCachePut(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Добавление в кэш: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "execution(* com.movio.moviolab.cache.InMemoryCache.remove(..))")
    public void logCacheRemove(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Удаление из кэша: {} с аргументами: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "execution(* com.movio.moviolab"
            + ".cache.InMemoryCache.get(..))", returning = "result")
    public void logCacheGet(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Чтение из кэша: {} с аргументами: {}, результат: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs(), result);
        }
    }
}
