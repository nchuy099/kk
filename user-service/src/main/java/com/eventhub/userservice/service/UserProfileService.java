package com.eventhub.userservice.service;

import com.eventhub.userservice.domain.UserProfile;
import com.eventhub.userservice.repository.UserProfileRepository;
import com.eventhub.userservice.service.exception.NotFoundException;
import com.eventhub.userservice.web.dto.AuthUser;
import com.eventhub.userservice.web.dto.CreateUserRequest;
import com.eventhub.userservice.web.dto.UpdateProfileRequest;
import com.eventhub.userservice.web.dto.UserProfileResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserProfileService {
    private final UserProfileRepository repository;
    private final KeycloakAdminClient keycloakAdminClient;

    public UserProfileService(UserProfileRepository repository, KeycloakAdminClient keycloakAdminClient) {
        this.repository = repository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Transactional
    public UserProfileResponse me(AuthUser authUser) {
        var profile = repository.findById(authUser.id())
                .orElseGet(() -> UserProfile.create(authUser.id(), authUser.username(), authUser.email(), authUser.username()));
        profile.syncIdentity(authUser.username(), authUser.email());
        return toResponse(repository.save(profile));
    }

    @Transactional
    public UserProfileResponse updateMe(AuthUser authUser, UpdateProfileRequest request) {
        me(authUser);
        var profile = find(authUser.id());
        profile.updateProfile(request.fullName(), request.phone());
        return toResponse(repository.save(profile));
    }

    public List<UserProfileResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UserProfileResponse get(String id) {
        return toResponse(find(id));
    }

    @Transactional
    public UserProfileResponse create(CreateUserRequest request) {
        var id = keycloakAdminClient.createUser(request);
        var profile = UserProfile.create(id, request.username(), request.email(), request.fullName());
        profile.updateProfile(request.fullName(), request.phone());
        return toResponse(repository.save(profile));
    }

    @Transactional
    public UserProfileResponse disable(String id) {
        var profile = find(id);
        keycloakAdminClient.disableUser(id);
        profile.disable();
        return toResponse(repository.save(profile));
    }

    private UserProfile find(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    private UserProfileResponse toResponse(UserProfile user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
