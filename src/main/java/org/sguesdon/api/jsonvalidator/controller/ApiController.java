package org.sguesdon.api.jsonvalidator.controller;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.io.CharStreams;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.sguesdon.api.jsonvalidator.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.exception.InvalidSchemaException;
import org.sguesdon.api.jsonvalidator.exception.NotFoundException;
import org.sguesdon.api.jsonvalidator.repository.ModelRepository;
import org.sguesdon.api.jsonvalidator.utils.JsonValidator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
class ApiController implements BaseController {

    private final ModelRepository modelRepository;
    private final MongoTemplate mongoTemplate;

    @GetMapping("/api/**")
    public StreamingResponseBody get(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "page", required = false, defaultValue="0") String page,
            @RequestParam(value = "size", required = false, defaultValue="25") String size
    ) throws NotFoundException, IOException {

        ModelDto model = this.findModel(request);

        if(!this.mongoTemplate.collectionExists(model.getCollection())) {
            return null;
        }

        response.setContentType("application/json");

        MongoCursor<Document> cursor = this.mongoTemplate
                .getCollection(model.getCollection())
                .find()
                .skip(Integer.parseInt(page) * Integer.parseInt(size))
                .limit(Integer.parseInt(size))
                .batchSize(100)
                .cursor();

        return outputStream -> {

            JsonFactory jfactory = new JsonFactory();
            JsonGenerator jGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8);

            jGenerator.writeStartObject();

            jGenerator.writeFieldName("data");
            jGenerator.writeStartArray();

            while (cursor.hasNext()) {
                Document document = cursor.next();
                document.put("_id", document.getObjectId("_id").toString());
                jGenerator.writeRaw(document.toJson());
            }

            cursor.close();
            jGenerator.writeEndArray();

            jGenerator.writeFieldName("pageable");
            jGenerator.writeStartObject();
            jGenerator.writeStringField("page", page);
            jGenerator.writeStringField("size", size);
            jGenerator.writeEndObject();

            jGenerator.writeEndObject();
            jGenerator.close();
        };
    }

    @PostMapping("/api/**")
    public ResponseEntity<?> post(HttpServletRequest request) throws NotFoundException, IOException, InvalidSchemaException, JSONException {

        final ModelDto model = this.findModel(request);
        final String body = CharStreams.toString(request.getReader());
        final MongoCollection<Document> collection = this.mongoTemplate.getCollection(model.getCollection());
        final Document record = Document.parse(body);

        JsonValidator.validate(model, body);

        collection.insertOne(record);

        record.put("_id", record.getObjectId("_id").toString());

        return new ResponseEntity<>(
            record.toJson(),
            HttpStatus.CREATED
        );
    }

    @PutMapping("/api/{modelName:.*}/{id}")
    public ResponseEntity<?> put(
        HttpServletRequest request,
        @PathVariable String modelName,
        @PathVariable String id
    ) throws NotFoundException, IOException, InvalidSchemaException {

        final ModelDto model = this.findModel(modelName);
        final String body = CharStreams.toString(request.getReader());

        JsonValidator.validate(model, body);

        final Document record = Document.parse(body);
        final MongoCollection<Document> collection = this.mongoTemplate.getCollection(model.getCollection());

        final Bson condition = Filters.eq("_id", new ObjectId(id));
        Document update = new Document();
        update.append("$set", record);

        final UpdateResult res = collection.updateOne(condition, update);

        if(res.getMatchedCount() == 1 && res.getModifiedCount() == 1) {
            return ResponseEntity.noContent().build();
        }

        throw this.notFoundException(id);
    }

    @DeleteMapping("/api/{modelName:.*}/{id}")
    public ResponseEntity<?> delete(
        @PathVariable String modelName,
        @PathVariable String id
    ) throws NotFoundException {

        final ModelDto model = this.findModel(modelName);
        final MongoCollection<Document> collection = this.mongoTemplate.getCollection(model.getCollection());
        final Bson condition = Filters.eq("_id", new ObjectId(id));
        final DeleteResult res = collection.deleteOne(condition);

        if(res.getDeletedCount() == 1) {
            return ResponseEntity.ok().build();
        }

        throw this.notFoundException(id);
    }

    private ModelDto findModel(HttpServletRequest request) throws NotFoundException {
        final String requestURL = request.getRequestURL().toString();
        final String endpoint = requestURL.split("/api/")[1];
        return this.findModel(endpoint);
    }

    private ModelDto findModel(String endpoint) throws NotFoundException {
        return this.modelRepository.findOneByEndpoint(endpoint).orElseThrow(() -> this.notFoundException(endpoint));
    }
}
