package org.sguesdon.api.jsonvalidator.models.domain.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "models")
public class ModelDto {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "id")
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "tag")
    String tag;
}
