package jv.supermarket.user;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jv.supermarket.cart.Cart;
import jv.supermarket.cart.CartRepository;
import jv.supermarket.shared.customexception.AlreadyExistException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@Service
public class UserService {

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
        if (existsByEmail(user.getEmail())) {
            throw new AlreadyExistException("A user with this email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_CLIENTE");
        user.getRoles().add(role);

        user = userRepository.save(user);

        if (user.getCart() == null) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        return user;
    }

    @Transactional
    public User saveAdmin(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new AlreadyExistException("A user with this email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_ADMIN");
        user.getRoles().add(role);

        return userRepository.save(user);
    }

    @Transactional
    public User saveEmployee(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new AlreadyExistException("A user with this email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName("ROLE_FUNCIONARIO");
        user.getRoles().add(role);

        user = userRepository.save(user);

        if (user.getCart() == null) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        return user;
    }

    public User getLoggedUser() {
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
        throw new ResourceNotFoundException("Logged user not found");
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User getByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User updateUser(User user, Long userId) {
        if (existById(userId)) {
            User existingUser = getById(userId);

            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());

            return userRepository.save(existingUser);
        } else {
            throw new ResourceNotFoundException("No user found with id: " + userId);
        }
    }

    public void deleteUser(Long userId) {
        if (existById(userId)) {
            userRepository.deleteById(userId);
        } else {
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
                .anyMatch(role -> role.getName().equals("ROLE_CLIENTE"));
    }

}
