package org.sguesdon.api.jsonvalidator.models.controller;

import lombok.RequiredArgsConstructor;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.models.domain.mapper.ModelMapperImpl;
import org.sguesdon.api.jsonvalidator.models.repository.ModelRepository;
import org.sguesdon.api.jsonvalidator.openapi.api.ModelsApiDelegate;
import org.sguesdon.api.jsonvalidator.openapi.model.Model;
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
        return ResponseEntity.ok(mapper.fromDtos(repo.findAll(paging).toList()));
    }

    public ResponseEntity<Model> createModels(Model model) {

        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(model.getSchema()));
        } catch (JSONException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
    }

    public ResponseEntity<Model> updateModelById(String modelId, Model model) {
        if(this.repo.existsById(modelId)) {

            try {
                JSONObject jsonSchema = new JSONObject(new JSONTokener(model.getSchema()));
            } catch (JSONException e) {
                return ResponseEntity.badRequest().build();
            }

            model.setId(modelId);
            return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Void> deleteModelById(String modelId) {
        if(this.repo.existsById(modelId)) {
            this.repo.deleteById(modelId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Model> showModelById(String modelId) {
        Optional<ModelDto> modelDtoOptional = repo.findById(modelId);
        return modelDtoOptional
            .map(modelDto -> ResponseEntity.ok(mapper.fromDto(modelDto)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> validateDataWithModel(String modelId, String body) {

        Optional<ModelDto> modelDtoOptional = repo.findById(modelId);

        if(modelDtoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(modelDtoOptional.get().getSchema()));
            JSONObject jsonSubject = new JSONObject(new JSONTokener(body));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
            return ResponseEntity.noContent().build();
        } catch (JSONException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
