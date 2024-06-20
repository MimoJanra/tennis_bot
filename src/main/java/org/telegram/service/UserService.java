package org.telegram.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.models.User;
import org.telegram.repository.UserRepository;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Value("#{'${bot.admin-usernames}'.split(',')}")
    private List<String> adminUsernames;

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public User createOrUpdate(Update update) {
        String username;
        Long chatId;

        if (update.hasMessage()) {
            username = update.getMessage().getFrom().getUserName();
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            username = update.getCallbackQuery().getFrom().getUserName();
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            throw new IllegalArgumentException("Update does not contain a message or a callback query");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
            user.setChatId(chatId);
            logger.info("User found and updated: userId: {}, userName: {}, chatId: {}", user.getId(), user.getUsername(), user.getChatId());
        } else {
            user = new User();
            user.setUsername(username);
            user.setChatId(chatId);
            user.setAdmin(adminUsernames.contains(username));
            logger.info("New user created: userName: {}, chatId: {}, isAdmin: {}", user.getUsername(), user.getChatId(), user.isAdmin());
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateChatId(String username, Long chatId) {
        Optional<User> userOpt = findByUsername(username);
        userOpt.ifPresent(user -> {
            user.setChatId(chatId);
            userRepository.save(user);
            logger.info("ChatId updated for userName: {}, new chatId: {}", username, chatId);
        });
    }
}
