package com.codessquad.qna.controller;

import com.codessquad.qna.domain.User;
import com.codessquad.qna.exception.*;
import com.codessquad.qna.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String userList(Model model) {
        model.addAttribute("users", userService.getUserList());
        return "users/list";
    }

    @PostMapping()
    public String registerUser(User user) {
        userService.register(user);
        return "redirect:/users";
    }

    @GetMapping("/{id}")
    public String userProfile(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/profile";
    }

    @GetMapping("/{id}/form")
    public String updateUserForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        checkSessionWithId(session, id);
        model.addAttribute(userService.getUserById(id));
        return "users/updateForm";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable("id") Long id, String oldPassword, User newUserInfo, HttpSession session) {
        checkSessionWithId(session, id);
        userService.updateUserInfo(id, oldPassword, newUserInfo);
        return "redirect:/users";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "users/login";
    }

    @PostMapping("/login")
    public String login(String userId, String password, HttpSession session) {
        User user = userService.authenticate(userId, password);
        HttpSessionUtils.setUserSession(session, user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        HttpSessionUtils.removeUserSession(session);
        return "redirect:/";
    }

    private void checkSessionWithId(HttpSession session, Long accessId) {
        User sessionUser = HttpSessionUtils.getUserFromSession(session);
        if (!sessionUser.isMatchingId(accessId)) {
            throw new UnauthorizedAccessException("다른 사람의 정보를 수정할 수 없습니다.");
        }
    }
}
