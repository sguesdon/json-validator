package org.sguesdon.api.jsonvalidator.models.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("models")
public class ModelDto {
    @Id
    String id;
    String name;
    String tag;
    String schema;
}
