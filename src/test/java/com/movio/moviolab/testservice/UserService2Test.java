package com.movio.moviolab.testservice;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.User;
import com.movio.moviolab.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserService2Test {
    @Mock
    private UserDao userDao;

    @Mock
    private InMemoryCache inMemoryCache;

    @InjectMocks
    private UserService userService;

    private User user;

    private static final Logger log = LoggerFactory.getLogger(UserService2Test.class);

    @BeforeEach
    void setUp() {
        try {
            MockitoAnnotations.openMocks(this);
            user = new User();
            user.setId(1);
            user.setName("John Doe");
            user.setEmail("john.doe@example.com");
            user.setPassword("password123");
        } catch (Exception e) {
            log.error("Error setting up mocks", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUsersByGenreFromCacheOrDb_CacheHit() {
        String genre = "Action";
        List<UserDto> cachedUsers = List.of(new UserDto());
        when(inMemoryCache.contains("movie_genre_" + genre)).thenReturn(true);
        when(inMemoryCache.get("movie_genre_" + genre)).thenReturn(Optional.of(cachedUsers));

        List<UserDto> result = userService.getUsersByGenreFromCacheOrDb(genre, mock(Function.class));

        assertNotNull(result);
        assertEquals(cachedUsers, result);
        verify(inMemoryCache, times(1)).contains("movie_genre_" + genre);
        verify(inMemoryCache, times(1)).get("movie_genre_" + genre);
    }

    @Test
    void testGetUsersByGenreFromCacheOrDb_CacheMiss() {
        String genre = "Action";

        User newUser = new User();
        newUser.setId(1);
        newUser.setName("John Doe");
        newUser.setEmail("john.doe@example.com");

        UserDto expectedDto = new UserDto();
        expectedDto.setId(newUser.getId());
        expectedDto.setName(newUser.getName());
        expectedDto.setEmail(newUser.getEmail());

        List<User> usersFromDb = List.of(newUser);

        when(inMemoryCache.contains("movie_genre_" + genre)).thenReturn(false);
        when(userDao.findUsersByMovieGenre(genre)).thenReturn(usersFromDb);
        when(inMemoryCache.get("movie_genre_" + genre)).thenReturn(Optional.empty());

        Function<String, List<User>> findUsersByGenreFunction = genre1 -> usersFromDb;

        List<UserDto> result = userService.getUsersByGenreFromCacheOrDb(genre, findUsersByGenreFunction);

        assertNotNull(result);
        assertEquals(List.of(expectedDto), result);
        verify(inMemoryCache, times(1)).put("movie_genre_" + genre, List.of(expectedDto));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUsersByGenreFromCacheOrDb_CacheDataInvalidType() {
        String genre = "Action";
        List<?> invalidData = List.of(new Object());
        when(inMemoryCache.contains("movie_genre_" + genre)).thenReturn(true);
        when(inMemoryCache.get("movie_genre_" + genre)).thenReturn(Optional.of(invalidData));

        List<UserDto> result = userService.getUsersByGenreFromCacheOrDb(genre, mock(Function.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inMemoryCache, times(1)).contains("movie_genre_" + genre);
        verify(inMemoryCache, times(1)).get("movie_genre_" + genre);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetUsersByGenreFromCacheOrDb_CacheDataEmpty() {
        String genre = "Action";
        List<UserDto> emptyCache = List.of();
        when(inMemoryCache.contains("movie_genre_" + genre)).thenReturn(true);
        when(inMemoryCache.get("movie_genre_" + genre)).thenReturn(Optional.of(emptyCache));

        List<UserDto> result = userService.getUsersByGenreFromCacheOrDb(genre, mock(Function.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inMemoryCache, times(1)).contains("movie_genre_" + genre);
        verify(inMemoryCache, times(1)).get("movie_genre_" + genre);
    }

    @Test
    void testValidateUser_Email() {
        UserDto partialUserDto = new UserDto();
        partialUserDto.setEmail("shizognomegmail.com");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.patchUser(1, partialUserDto));

        assertEquals("Новый пользователь *invalid*", exception.getMessage());
    }

    @Test
    void testValidateUser_Password() {
        UserDto updateUserDto = new UserDto();
        updateUserDto.setName("Jonson");
        updateUserDto.setEmail("jonson@example.com");
        updateUserDto.setPassword("");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.updateUser(1, updateUserDto));

        assertEquals("Пароль обязателен", exception.getMessage());
    }

    @Test
    void testValidateUser_InvalidName() {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("A");
        invalidUserDto.setEmail("valid.email@example.com");
        invalidUserDto.setPassword("password123");

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(invalidUserDto));

        assertEquals("Имя обязательно и должно быть от 2 до 50 символов", exception.getMessage());
    }

    @Test
    void testValidateUser_InvalidEmail() {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("Valid Name");
        invalidUserDto.setEmail("invalid-email");
        invalidUserDto.setPassword("password123");

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(invalidUserDto));

        assertEquals("Почта не введена или не соблюдается формат адреса", exception.getMessage());
    }

    @Test
    void testValidateUser_InvalidPassword() {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("Valid Name");
        invalidUserDto.setEmail("valid.email@example.com");
        invalidUserDto.setPassword("123");

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.addUser(invalidUserDto));

        assertEquals("Длинна пароля должна быть от 5 до 20 символов", exception.getMessage());
    }

    @Test
    void testGetCommentsByUserId_Success() {
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        List<CommentDto> comments = userService.getCommentsByUserId(1);

        assertNotNull(comments);
    }

    @Test
    void testGetCommentsByUserId_UserNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.getCommentsByUserId(1));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testGetUsersByNameAndEmail() {
        String name = "John";
        String email = "john@example.com";
        List<User> mockUsers = new ArrayList<>();

        User mokUser = new User();
        mokUser.setName(name);
        mokUser.setEmail(email);
        mockUsers.add(mokUser);

        when(userDao.findByNameIgnoreCaseAndEmailIgnoreCase(name, email)).thenReturn(mockUsers);

        List<UserDto> result = userService.getUsers(name, email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name, result.getFirst().getName());
        assertEquals(email, result.getFirst().getEmail());
    }

    @Test
    void testGetUsersByNameOnly() {
        String name = "John";
        List<User> mockUsers = new ArrayList<>();
        User mokUser = new User();

        mokUser.setName(name);
        mockUsers.add(mokUser);

        when(userDao.findByNameIgnoreCase(name)).thenReturn(mockUsers);

        List<UserDto> result = userService.getUsers(name, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name, result.getFirst().getName());
    }

    @Test
    void testGetUsersByEmailOnly() {
        String email = "john@example.com";
        List<User> mockUsers = new ArrayList<>();
        User mokUser = new User();
        mokUser.setEmail(email);
        mockUsers.add(mokUser);

        when(userDao.findByEmailIgnoreCase(email)).thenReturn(mockUsers);

        List<UserDto> result = userService.getUsers(null, email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(email, result.getFirst().getEmail());
    }

    @Test
    void testGetAllUsers() {
        List<User> mockUsers = new ArrayList<>();

        User mokUser1 = new User();
        mokUser1.setName("John");
        mokUser1.setEmail("john@example.com");
        mockUsers.add(mokUser1);

        User mokUser2 = new User();
        mokUser2.setName("Jane");
        mokUser2.setEmail("jane@example.com");
        mockUsers.add(mokUser2);

        when(userDao.findAll()).thenReturn(mockUsers);

        List<UserDto> result = userService.getUsers(null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetUsers() {
        when(userDao.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.getUsers(null, null);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John Doe", users.getFirst().getName());
    }

    @Test
    void testGetUsers_UserNotFound() {
        when(userDao.findAll()).thenReturn(List.of());

        UserException exception = assertThrows(UserException.class, () -> userService.getUsers(null, null));

        assertEquals("Пользователь не найден: пользователь с такими параметрами не существует", exception.getMessage());
    }
}
