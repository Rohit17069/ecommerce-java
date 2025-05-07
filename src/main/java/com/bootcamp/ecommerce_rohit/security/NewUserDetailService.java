package com.bootcamp.ecommerce_rohit.security;

import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public NewUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                !user.getIsLocked(),
                List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().getAuthority().toUpperCase()))
        );
    }
}
