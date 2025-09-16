package com.fintory.auth.service;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.domain.common.User;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ChildRepository childRepository;

    @Override                            //email(공통)
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {

        Optional<User> userOptional = childRepository.findByEmail(id)
                .<User>map(child -> child);

        return userOptional.map(user -> new CustomUserDetails(
                        user.getEmail(),
                        user.getPassword(), //소셜의 경우 null 리턴
                        user.getNickname(),
                        user.getRole().getKey(),
                        user.getLoginType()))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }

}