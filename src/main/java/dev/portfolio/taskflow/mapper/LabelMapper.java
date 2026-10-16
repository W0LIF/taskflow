package dev.portfolio.taskflow.mapper;

import dev.portfolio.taskflow.dto.response.LabelResponse;
import dev.portfolio.taskflow.model.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {
    public LabelResponse toResponse(Label label){
        return new LabelResponse(label.getId(), label.getName(), label.getColor());
    }
}
