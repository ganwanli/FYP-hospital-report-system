package com.hospital.report.service;

import com.hospital.report.entity.User;
import com.hospital.report.repository.UserRepository;
import com.hospital.report.dto.RegisterRequest;
import com.hospital.report.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRealName("测试用户");
        testUser.setRole("USER");
        testUser.setStatus("ACTIVE");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setRealName("新用户");
    }

    @Test
    @DisplayName("根据用户名查找用户")
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User found = userService.findByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("用户名不存在")
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("notexist")).thenReturn(Optional.empty());

        User found = userService.findByUsername("notexist");

        assertNull(found);
        verify(userRepository).findByUsername("notexist");
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void testExistsByUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByUsername("notexist")).thenReturn(false);

        assertTrue(userService.existsByUsername("testuser"));
        assertFalse(userService.existsByUsername("notexist"));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByUsername("notexist");
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("notexist@example.com")).thenReturn(false);

        assertTrue(userService.existsByEmail("test@example.com"));
        assertFalse(userService.existsByEmail("notexist@example.com"));

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByEmail("notexist@example.com");
    }

    @Test
    @DisplayName("创建用户成功")
    void testCreateUser() {
        // Mock dependencies
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedNewPassword");
        
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("$2a$10$encodedNewPassword");
        savedUser.setEmail("newuser@example.com");
        savedUser.setRealName("新用户");
        savedUser.setRole("USER");
        savedUser.setStatus("ACTIVE");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User created = userService.createUser(registerRequest);

        assertNotNull(created);
        assertEquals("newuser", created.getUsername());
        assertEquals("newuser@example.com", created.getEmail());
        assertEquals("新用户", created.getRealName());
        assertEquals("USER", created.getRole());
        assertEquals("ACTIVE", created.getStatus());

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户失败 - 用户名已存在")
    void testCreateUserUsernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(registerRequest);
        });

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户失败 - 邮箱已存在")
    void testCreateUserEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(registerRequest);
        });

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("获取所有用户")
    void testFindAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("根据ID查找用户")
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User found = userService.findById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("testuser", found.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID查找用户 - 不存在")
    void testFindByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User found = userService.findById(999L);

        assertNull(found);
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("更新用户状态")
    void testUpdateUserStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateStatus(1L, "INACTIVE");

        assertNotNull(updated);
        assertEquals("INACTIVE", updated.getStatus());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("更新用户角色")
    void testUpdateUserRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateRole(1L, "ADMIN");

        assertNotNull(updated);
        assertEquals("ADMIN", updated.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("删除用户")
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.delete(1L));

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除用户 - 用户不存在")
    void testDeleteUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.delete(999L);
        });

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("根据角色查找用户")
    void testFindByRole() {
        List<User> adminUsers = Arrays.asList(testUser);
        when(userRepository.findByRole("ADMIN")).thenReturn(adminUsers);

        List<User> result = userService.findByRole("ADMIN");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByRole("ADMIN");
    }

    @Test
    @DisplayName("根据状态查找用户")
    void testFindByStatus() {
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findByStatus("ACTIVE")).thenReturn(activeUsers);

        List<User> result = userService.findByStatus("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByStatus("ACTIVE");
    }
}