package top.naccl.dwz.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.dwz.admin.service.AuthService;
import top.naccl.dwz.common.dto.ApiResponse;
import top.naccl.dwz.common.dto.request.LoginRequest;
import top.naccl.dwz.common.dto.request.RegisterRequest;
import top.naccl.dwz.common.dto.response.LoginResponse;
import top.naccl.dwz.common.dto.response.UserVO;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
