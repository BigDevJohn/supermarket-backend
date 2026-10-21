package jv.supermarket.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.cart.Cart;
import jv.supermarket.cart.CartRepository;
import jv.supermarket.shared.customexception.BadAuthRequestException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final CartRepository cartRepository;

    private final RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, CartRepository cartRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User saveClient(User user) {
        log.info("[saveClient] Registering new customer");
        if (existsByEmail(user.getEmail())) {
            log.warn("[saveClient] Registration failed: email already exists");
            throw new BadAuthRequestException("Registration failed: invalid data provided");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        user.getRoles().add(role);

        user = userRepository.save(user);

        if (user.getCart() == null) {
            log.info("[saveClient] Initializing cart for customer ID: {}", user.getId());
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        log.info("[saveClient] Customer registered successfully with ID: {}", user.getId());
        return user;
    }

    @Transactional
    public User saveAdmin(User user) {
        log.info("[saveAdmin] Registering new admin");
        if (existsByEmail(user.getEmail())) {
            log.warn("[saveAdmin] Registration failed: email already exists");
            throw new BadAuthRequestException("Registration failed: invalid data provided");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_ADMIN");
        user.getRoles().add(role);

        User savedUser = userRepository.save(user);
        log.info("[saveAdmin] Admin registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public User saveEmployee(User user) {
        log.info("[saveEmployee] Registering new employee");
        if (existsByEmail(user.getEmail())) {
            log.warn("[saveEmployee] Registration failed: email already exists");
            throw new BadAuthRequestException("Registration failed: invalid data provided");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_EMPLOYEE");
        user.getRoles().add(role);

        user = userRepository.save(user);

        if (user.getCart() == null) {
            log.info("[saveEmployee] Initializing cart for employee ID: {}", user.getId());
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        log.info("[saveEmployee] Employee registered successfully with ID: {}", user.getId());
        return user;
    }

    public User getLoggedUser() {
        log.debug("[getLoggedUser] Fetching currently authenticated user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                return getByEmail(springUser.getUsername());
            }

            if (principal instanceof User user) {
                return user;
            }
        }
        log.warn("[getLoggedUser] No authenticated user found in SecurityContext");
        throw new ResourceNotFoundException("Logged user not found");
    }

    public User getById(Long userId) {
        log.info("[getById] Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[getById] User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
    }

    public User getByEmail(String email) {
        log.debug("[getByEmail] Fetching user by email");
        return Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> {
                    log.warn("[getByEmail] User not found by email");
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    public User updateUser(User user, Long userId) {
        log.info("[updateUser] Updating user with ID: {}", userId);
        if (existById(userId)) {
            User existingUser = getById(userId);

            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());

            User updated = userRepository.save(existingUser);
            log.info("[updateUser] User with ID: {} updated successfully", updated.getId());
            return updated;
        } else {
            log.warn("[updateUser] No user found with id: {}", userId);
            throw new ResourceNotFoundException("No user found with id: " + userId);
        }
    }

    public void deleteUser(Long userId) {
        log.info("[deleteUser] Attempting to delete user with ID: {}", userId);
        if (existById(userId)) {
            userRepository.deleteById(userId);
            log.info("[deleteUser] User with ID: {} deleted successfully", userId);
        } else {
            log.warn("[deleteUser] No user found with id: {}", userId);
            throw new ResourceNotFoundException("No user found with id: " + userId);
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private boolean existById(Long id) {
        return userRepository.existsById(id);
    }

    public boolean isClient() {
        User user = getLoggedUser();
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_CUSTOMER"));
    }

}
