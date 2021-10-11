package org.sguesdon.api.jsonvalidator.models.controller;

import lombok.RequiredArgsConstructor;
import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.models.domain.mapper.ModelMapperImpl;
import org.sguesdon.api.jsonvalidator.openapi.api.ModelsApiDelegate;
import org.sguesdon.api.jsonvalidator.openapi.model.Model;
import org.sguesdon.api.jsonvalidator.models.repository.ModelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ModelsController implements ModelsApiDelegate {

    private final ModelMapperImpl mapper;
    private final ModelRepository repo;

    public ResponseEntity<List<Model>> listModels(Integer page, Integer size) {
        final Pageable paging = PageRequest.of(page, size);
        return ResponseEntity.ok(mapper.fromDtos(repo.findAll(paging)));
    }

    public ResponseEntity<Model> createModels(Model model) {
        return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
    }

    public ResponseEntity<Model> updateModelById(Long modelId, Model model) {
        if(this.repo.existsById(modelId)) {
            model.setId(modelId);
            return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Void> deleteModelById(Long modelId) {
        if(this.repo.existsById(modelId)) {
            this.repo.deleteById(modelId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Model> showModelById(Long modelId) {
        Optional<ModelDto> modelDtoOptional = repo.findById(modelId);
        return modelDtoOptional
            .map(modelDto -> ResponseEntity.ok(mapper.fromDto(modelDto)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
