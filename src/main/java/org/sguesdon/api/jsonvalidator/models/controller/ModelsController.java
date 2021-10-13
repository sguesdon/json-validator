package org.sguesdon.api.jsonvalidator.models.controller;

import lombok.RequiredArgsConstructor;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.models.domain.mapper.ModelMapperImpl;
import org.sguesdon.api.jsonvalidator.models.exception.InvalidSchemaException;
import org.sguesdon.api.jsonvalidator.models.exception.NotFoundException;
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

    public ResponseEntity<Model> createModels(Model model) throws InvalidSchemaException {

        try {
            new JSONObject(new JSONTokener(model.getSchema()));
        } catch(JSONException exception) {
            throw new InvalidSchemaException("invalid json schema");
        }

        return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
    }

    public ResponseEntity<Model> updateModelById(String modelId, Model model) throws InvalidSchemaException, NotFoundException {

        if(!this.repo.existsById(modelId)) {
            throw new NotFoundException(String.format("model with id %s doesn't exist", modelId));
        }

        try {
            new JSONObject(new JSONTokener(model.getSchema()));
        } catch(JSONException exception) {
            throw new InvalidSchemaException("invalid json schema");
        }

        model.setId(modelId);
        return ResponseEntity.ok(mapper.fromDto(repo.save(mapper.toDto(model))));
    }

    public ResponseEntity<Void> deleteModelById(String modelId) throws NotFoundException {

        if(!this.repo.existsById(modelId)) {
            throw new NotFoundException(String.format("model with id %s doesn't exist", modelId));
        }

        this.repo.deleteById(modelId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Model> showModelById(String modelId) throws NotFoundException {
        Optional<ModelDto> modelDtoOptional = repo.findById(modelId);
        return modelDtoOptional
            .map(modelDto -> ResponseEntity.ok(mapper.fromDto(modelDto)))
            .orElseThrow(() -> new NotFoundException(String.format("model with id %s doesn't exist", modelId)));
    }

    public ResponseEntity<Void> validateDataWithModel(String modelId, String body) throws NotFoundException, InvalidSchemaException {

        ModelDto modelDto = repo.findById(modelId)
            .orElseThrow(() -> new NotFoundException(String.format("model with id %s doesn't exist", modelId)));

        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(modelDto.getSchema()));
            JSONObject jsonSubject = new JSONObject(new JSONTokener(body));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
            return ResponseEntity.noContent().build();
        } catch(JSONException exception) {
            throw new InvalidSchemaException("json not valid");
        }
    }
}
