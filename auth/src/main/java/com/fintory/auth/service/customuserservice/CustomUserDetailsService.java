package com.fintory.auth.service.customuserservice;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.infra.domain.child.repository.ChildRepository;
import com.fintory.infra.domain.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.fintory.domain.child.model.LoginType.EMAIL;

@Service
@RequiredArgsConstructor //소셜로그인은 안씀, 걍 주체 구분해서 ud 값 리턴하면 됨
public class CustomUserDetailsService implements UserDetailsService {

    private final ChildRepository childRepository;
    private final ParentRepository parentRepository;

    @Override                            //email(공통)
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