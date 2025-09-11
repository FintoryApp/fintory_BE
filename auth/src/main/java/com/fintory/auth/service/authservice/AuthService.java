package com.fintory.auth.service.authservice;

import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.request.SignUpRequest;

public interface AuthService {

    AuthToken signup(SignUpRequest request);

    boolean checkDuplicateEmail(String email);

    AuthToken login(String email, String password);

    AuthToken reissue(String refreshToken);

    void logout(String username);
}
