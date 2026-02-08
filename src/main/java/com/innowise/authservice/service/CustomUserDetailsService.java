package com.innowise.authservice.service;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.UserNotFoundException;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final AuthUserRepository authUserRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    AuthUser authUser = authUserRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("email", email));
    return new AuthUserDetails(authUser);
  }
}
