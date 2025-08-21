package com.fintory.auth.service;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.fintory.domain.child.model.LoginType.EMAIL;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ChildRepository childRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return childRepository.findBySocialId(id)
                .or(() -> childRepository.findByEmail(id))
                .map(child -> new CustomUserDetails(
                        child.getLoginType() == EMAIL ? child.getEmail() : child.getSocialId(),
                        child.getPassword(),
                        child.getNickname(),
                        child.getRole().getKey(),
                        child.getLoginType()))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다" + id));
    }

}