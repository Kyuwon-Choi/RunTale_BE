package com.likelion.runtale.domain.user.controller;

import com.likelion.runtale.common.ApiResponse;
import com.likelion.runtale.common.response.SuccessMessage;
import com.likelion.runtale.domain.user.dto.UserRequest;
import com.likelion.runtale.domain.user.dto.UserResponse;
import com.likelion.runtale.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "user", description = "user 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable("id") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessage.USER_INFO_FIND_SUCCESS, user));
    }

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest userRequest) {
        UserResponse user = userService.createUser(userRequest);
        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(ApiResponse.success(SuccessMessage.USER_SIGNUP_SUCCESS, user));
    }

    @Operation(summary = "회원 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest userRequest) {
        UserResponse user = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessage.USER_INFO_CHANGE_SUCCESS, user));
    }


    @Operation(summary = "회원 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessage.USER_INFO_CHANGE_SUCCESS));
    }

}
