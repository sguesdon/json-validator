package org.sguesdon.api.jsonvalidator.models.repository;

import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends CrudRepository<ModelDto, Long> {
    List<ModelDto> findAll(Pageable paging);
}
