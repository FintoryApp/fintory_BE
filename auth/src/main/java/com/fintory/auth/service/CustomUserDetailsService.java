package com.fintory.auth.service;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ChildRepository childRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return childRepository.findByEmail(email)
                .map(child -> new CustomUserDetails(child.getEmail(), child.getPassword(), child.getNickname(), child.getRole().getKey()))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

}