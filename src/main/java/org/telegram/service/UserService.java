package org.telegram.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.bot.UpdateUtil;
import org.telegram.models.User;
import org.telegram.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    @Value("${bot.admin-usernames}")
    private String adminUsernames;

    private final UserRepository userRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(Update update) {
        List<String> adminUsernamesList = List.of(adminUsernames.split(","));
        User user = new User();
        user.setId(UpdateUtil.getUserId(update));

        String username = update.getMessage().getFrom().getUserName();
        user.setUsername(username);
        user.setAdmin(adminUsernamesList.contains(username));
        userRepository.save(user);

        return user;
    }

    @PostConstruct
    private void setAdmins() {
        List<String> adminUsernamesList = List.of(adminUsernames.split(","));
        List<User> usersList = (List<User>) userRepository.findAll();
        for (User user : usersList) {
            user.setAdmin(adminUsernamesList.contains(user.getUsername()));
        }

        userRepository.saveAll(usersList);
    }
}