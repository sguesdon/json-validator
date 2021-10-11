package org.sguesdon.api.jsonvalidator.models.repository;

import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends MongoRepository<ModelDto, String> {
    Page<ModelDto> findAll(Pageable paging);
}
