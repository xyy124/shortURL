package top.naccl.dwz.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.naccl.dwz.common.dto.request.LoginRequest;
import top.naccl.dwz.common.dto.request.RegisterRequest;
import top.naccl.dwz.common.dto.response.LoginResponse;
import top.naccl.dwz.common.dto.response.UserVO;
import top.naccl.dwz.common.entity.User;
import top.naccl.dwz.common.enums.ResultCode;
import top.naccl.dwz.common.exception.ApiException;
import top.naccl.dwz.common.security.JwtTokenProvider;
import top.naccl.dwz.core.mapper.UserMapper;

@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserVO register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new ApiException(ResultCode.CONFLICT.getCode(), "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setIsActive(true);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.UNAUTHORIZED);
        }
        String token = jwtTokenProvider.generateToken(user);
        return new LoginResponse(token, UserVO.from(user));
    }
}
