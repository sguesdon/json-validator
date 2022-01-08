package org.sguesdon.api.jsonvalidator.repository;

import org.sguesdon.api.jsonvalidator.domain.entity.ModelDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRepository extends MongoRepository<ModelDto, String> {
    Page<ModelDto> findAll(Pageable paging);
    Optional<ModelDto> findOneByEndpoint(String endpoint);
}
