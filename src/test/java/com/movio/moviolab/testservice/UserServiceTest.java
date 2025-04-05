package com.movio.moviolab.testservice;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import com.movio.moviolab.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private InMemoryCache inMemoryCache;

    @InjectMocks
    private UserService userService;

    private User user;

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

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

    @Test
    void testGetUserById_UserExists() {
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(1);

        assertNotNull(userDto);
        assertEquals("John Doe", userDto.getName());
        assertEquals("john.doe@example.com", userDto.getEmail());
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.getUserById(1));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testAddUser_Success() {
        UserDto validUserDto = new UserDto();
        validUserDto.setName("Valid Name");
        validUserDto.setEmail("valid.email@example.com");
        validUserDto.setPassword("validpassword123");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setName("Valid Name");
        savedUser.setEmail("valid.email@example.com");
        savedUser.setPassword("validpassword123");

        when(userDao.findByNameIgnoreCase(validUserDto.getName())).thenReturn(emptyList());
        when(userDao.findByEmailIgnoreCase(validUserDto.getEmail())).thenReturn(emptyList());
        when(userDao.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.addUser(validUserDto);

        assertNotNull(result);
        assertEquals("Valid Name", result.getName());
        assertEquals("valid.email@example.com", result.getEmail());
        assertEquals(1, result.getId());
    }

    @Test
    void testAddUser_UserNameExists() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPassword("password123");

        User userTest = new User();
        userTest.setName("John Doe");
        userTest.setEmail("john@example.com");

        when(userDao.findByNameIgnoreCase(userTest.getName())).thenReturn(List.of(userTest));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.addUser(userDto));
        assertEquals("Пользователь с таким именем и почтой уже существует: John Doe", exception.getMessage());
    }

    @Test
    void testAddUser_UserEmailExists() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPassword("password123");

        User userJohn = new User();
        userJohn.setName("John");
        userJohn.setEmail("john.doe@example.com");

        when(userDao.findByEmailIgnoreCase(user.getEmail())).thenReturn(List.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.addUser(userDto));
        assertEquals("Пользователь с таким именем и почтой уже существует: john.doe@example.com", exception.getMessage());
    }

    @Test
    void testDeleteUserById_Success() {
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userService.deleteUserById(1);

        verify(userDao, times(1)).deleteById(1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUserById_RemoveMoviesFromCache() {
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userService.deleteUserById(1);

        for (Movie movie : user.getMovies()) {
            String genre = movie.getGenre();

            String key = "movie_genre_" + genre;

            verify(inMemoryCache, times(1)).remove(key);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    @Test
    void testDeleteUserById_UserNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.deleteUserById(1));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setName("Updated Name");
        updatedUserDto.setEmail("updated.email@example.com");
        updatedUserDto.setPassword("newpassword123");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        UserDto updatedUser = userService.updateUser(1, updatedUserDto);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated.email@example.com", updatedUser.getEmail());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setName("Updated Name");
        updatedUserDto.setEmail("updated.email@example.com");
        updatedUserDto.setPassword("newpassword123");

        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.updateUser(1, updatedUserDto));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testPatchUser_Success() {
        UserDto partialUserDto = new UserDto();
        partialUserDto.setName("Partial Update");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        UserDto patchedUser = userService.patchUser(1, partialUserDto);

        assertNotNull(patchedUser);
        assertEquals("Partial Update", patchedUser.getName());
    }

    @Test
    void testPatchUser_Success2() {
        UserDto partialUserDto = new UserDto();
        partialUserDto.setEmail("shizognome@gmail.com");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        UserDto patchedUser = userService.patchUser(1, partialUserDto);

        assertNotNull(patchedUser);
        assertEquals("shizognome@gmail.com", patchedUser.getEmail());
    }

    @Test
    void testPatchUser_Success3() {
        UserDto partialUserDto = new UserDto();
        partialUserDto.setPassword("email1323");

        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.save(any())).thenReturn(user);

        UserDto patchedUser = userService.patchUser(1, partialUserDto);

        assertNotNull(patchedUser);
        assertNull(patchedUser.getPassword());
    }

    @Test
    void testPatchUser_UserNotFound() {
        UserDto partialUserDto = new UserDto();
        partialUserDto.setName("Partial Update");

        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.patchUser(1, partialUserDto));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        Integer userId = 1;
        User existingUser = new User();
        existingUser.setName("Jane");
        existingUser.setEmail("jane@example.com");
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        for (Movie movie : existingUser.getMovies()) {
            String genre = movie.getGenre();

            String key = "movie_genre_" + genre;

            verify(userDao, times(1)).deleteById(userId);
            verify(inMemoryCache, times(1)).remove(key);
        }
        ResponseEntity<String> result = userService.deleteUserById(userId);
        assertEquals(ResponseEntity.noContent().build(), result);
    }
}
