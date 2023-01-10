package com.example.pcswebserver.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.pcswebserver.data.RoleRepository;
import com.example.pcswebserver.data.UserRepository;
import com.example.pcswebserver.domain.Role;
import com.example.pcswebserver.domain.User;
import com.example.pcswebserver.exception.RoleNotFoundException;
import com.example.pcswebserver.exception.UserAlreadyExistsException;
import com.example.pcswebserver.exception.UserNotFoundException;
import com.example.pcswebserver.security.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void deleteByUsername(String username) {
        if (userRepository.existsByUsername(username))
            userRepository.deleteByUsername(username);
    }

    @Transactional
    public void signUp(String username, String password) {
        if (userRepository.existsByUsername(username))
            throw new UserAlreadyExistsException("User with username %s already exists" .formatted(username));
        var user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        var userRole = roleRepository.findByName(Role.USER)
                .orElseThrow(() -> new RoleNotFoundException("User with username %s already exists" .formatted(Role.USER)));
        user.setRole(userRole);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String username, String oldPassword, String newPassword) {
        var user = getByCredentials(username, oldPassword);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public Optional<DecodedJWT> signIn(String username, String password) {
        getByCredentials(username, password);
        return jwtProvider.toDecodedJWT(jwtProvider.generateToken(username));
    }

    private Optional<User> findByCredentials(String username, String password) {
        var optUser = userRepository.findByUsername(username);
        if (optUser.isPresent()) {
            var user = optUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return optUser;
            }
        }
        return Optional.empty();
    }

    private User getByCredentials(String username, String password) {
        return findByCredentials(username, password)
                .orElseThrow(() -> new UserNotFoundException("User with username %s not found" .formatted(username)));
    }

    @Autowired
    public void setJwtProvider(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


}