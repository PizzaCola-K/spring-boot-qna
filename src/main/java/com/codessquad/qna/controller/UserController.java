package com.codessquad.qna.controller;

import com.codessquad.qna.domain.User;
import com.codessquad.qna.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final Pattern userIdPattern = Pattern.compile("[1-9]\\d*");

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping()
    public ModelAndView userList() {
        ModelAndView modelAndView = new ModelAndView("users/list");
        modelAndView.addObject("users", userRepository.findAll());
        return modelAndView;
    }

    @PostMapping()
    public String registerUser(User user) {
        userRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/{id}")
    public ModelAndView userProfile(@PathVariable("id") String id) {
        Optional<User> user = matchIdPatternAndFindUser(id);
        if (!user.isPresent()) {
            return new ModelAndView("redirect:/users");
        }
        ModelAndView modelAndView = new ModelAndView("users/profile");
        modelAndView.addObject(user.get());
        return modelAndView;
    }

    @GetMapping("/{id}/form")
    public ModelAndView updateUserForm(@PathVariable("id") String id) {
        Optional<User> user = matchIdPatternAndFindUser(id);
        if (!user.isPresent()) {
            return new ModelAndView("redirect:/users");
        }
        ModelAndView modelAndView = new ModelAndView("users/update_form");
        modelAndView.addObject(user.get());
        return modelAndView;
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable("id") String id, String oldPassword, User newUserInfo) {
        matchIdPatternAndFindUser(id).ifPresent(u -> checkPasswordAndUpdate(u, oldPassword, newUserInfo));
        return "redirect:/users";
    }

    private void checkPasswordAndUpdate(User user, String oldPassword, User newUserInfo) {
        if (user.isMatchingPassword(oldPassword)) {
            user.update(newUserInfo);
            userRepository.save(user);
        }
    }

    private Optional<User> matchIdPatternAndFindUser(String id) {
        Matcher userIdMatcher = userIdPattern.matcher(id);
        if (!userIdMatcher.matches()) {
            return Optional.empty();
        }
        return userRepository.findById(Long.parseLong(id));
    }

    @GetMapping("/login")
    public String loginForm() {
        return "users/login";
    }

    @PostMapping("/login")
    public String login(String userId, String password, HttpSession session) {
        User user = userRepository.findByUserId(userId);
        if (user == null || !user.isMatchingPassword(password)) {
            return "users/login_failed";
        }
        session.setAttribute("sessionUser", user);
        return "redirect:/";
    }

}
