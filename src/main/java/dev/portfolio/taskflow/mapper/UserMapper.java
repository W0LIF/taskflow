package dev.portfolio.taskflow.mapper;

import dev.portfolio.taskflow.dto.response.UserSummaryResponse;
import dev.portfolio.taskflow.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserSummaryResponse toSummary(User user){
        if (user == null) return null;
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
        );
    }
}
