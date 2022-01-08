package org.sguesdon.api.jsonvalidator.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("models")
public class ModelDto {
    @Id
    String id;
    @Indexed(unique=true)
    String name;
    @Indexed(unique=true)
    String collection;
    @Indexed(unique=true)
    String endpoint;
    String version;
    String tag;
    String schema;
}
