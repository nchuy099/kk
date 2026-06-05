package com.eventhub.userservice.web;

import com.eventhub.userservice.service.UserProfileService;
import com.eventhub.userservice.web.dto.AuthUser;
import com.eventhub.userservice.web.dto.CreateUserRequest;
import com.eventhub.userservice.web.dto.UpdateProfileRequest;
import com.eventhub.userservice.web.dto.UserProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/users/me")
    public UserProfileResponse me(@RequestHeader("X-User-Id") String userId,
                                  @RequestHeader(value = "X-Username", required = false) String username,
                                  @RequestHeader(value = "X-User-Email", required = false) String email,
                                  @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        return userProfileService.me(new AuthUser(userId, username, email, roles));
    }

    @PatchMapping("/users/me")
    public UserProfileResponse updateMe(@RequestHeader("X-User-Id") String userId,
                                        @RequestHeader(value = "X-Username", required = false) String username,
                                        @RequestHeader(value = "X-User-Email", required = false) String email,
                                        @RequestHeader(value = "X-User-Roles", required = false) String roles,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateMe(new AuthUser(userId, username, email, roles), request);
    }

    @GetMapping("/admin/users")
    public List<UserProfileResponse> list() {
        return userProfileService.list();
    }

    @GetMapping("/admin/users/{id}")
    public UserProfileResponse get(@PathVariable String id) {
        return userProfileService.get(id);
    }

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userProfileService.create(request);
    }

    @PatchMapping("/admin/users/{id}/disable")
    public UserProfileResponse disable(@PathVariable String id) {
        return userProfileService.disable(id);
    }
}
