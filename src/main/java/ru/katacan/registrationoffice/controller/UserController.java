package ru.katacan.registrationoffice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.katacan.registrationoffice.dto.*;
import ru.katacan.registrationoffice.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMe(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getCurrentUser(authentication.getName()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/change-password")
    public ResponseEntity<SuccessResponseDto> changeMyPassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.changeOwnPassword(authentication.getName(), request));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar')")
    @GetMapping
    public ResponseEntity<UserListDto> getAllUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.searchUsers(search, pageable));
    }

    @PreAuthorize("hasAnyAuthority('admin', 'registrar')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDto> getUserById(
            @PathVariable Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(userId));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UpdateUserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateUser(userId, request));
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.deleteUser(userId));
    }

}