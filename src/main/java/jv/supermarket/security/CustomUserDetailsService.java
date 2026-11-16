package jv.supermarket.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jv.supermarket.user.User;
import jv.supermarket.user.UserService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("[loadUserByUsername] Loading user details for authentication");
        User user = userService.getByEmail(username);
        log.debug("[loadUserByUsername] User details successfully loaded for user ID: {}", user.getId());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.getRoles());
    }

}
