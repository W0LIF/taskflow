package dev.portfolio.taskflow.controller;

import tools.jackson.databind.ObjectMapper;
import dev.portfolio.taskflow.dto.request.ProjectCreateRequest;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.UserRepository;
import dev.portfolio.taskflow.security.JwtService;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProjectControllerIT extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired TestDataFactory factory;

    @Test
    void createProject_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_withValidToken_returns201() throws Exception {
        User user = factory.createRandomUser();
        String token = jwtService.generateToken(user);

        var body = new ProjectCreateRequest("Test Project", "desc");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.owner.id").value(user.getId()));
    }

    @Test
    void accessForeignProject_returns403() throws Exception {
        User owner = factory.createRandomUser();
        User stranger = factory.createRandomUser();
        var project = factory.createProject(owner);
        String strangerToken = jwtService.generateToken(stranger);

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("member")));
    }

    @Test
    void getProject_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/projects/1")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_validationErrors_return400WithDetails() throws Exception {
        User user = factory.createRandomUser();
        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}