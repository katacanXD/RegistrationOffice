package ru.katacan.registrationoffice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.mapper.UserMapper;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<UserListDto> getAllUsers(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.searchUsers(search));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserById(
            @PathVariable Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UpdateUserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.deleteUser(userId));
    }

    @PostMapping("/{userId}/change-password")
    public ResponseEntity<SuccessResponseDto> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.changePassword(userId, request));
    }
}