package org.sguesdon.api.jsonvalidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sguesdon.api.jsonvalidator.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ModelDto model;

    private String getUrl(String uri) {
        return "http://localhost:" + port + "/" + uri;
    }

    @BeforeEach
    public void beforeEach() throws IOException {

        this.repository.deleteAll();

        final ModelDto data = new ModelDto();
        data.setName("post_get");
        data.setTag("post_get");
        data.setCollection("ahah");
        data.setEndpoint("ahah");
        data.setSchema(Files.readString(Path.of("src/test/resources/json_schema_2.json")));
        this.model = this.repository.save(data);
    }

    @Test
    void findObjects() {

    }

    @Test
    void findObjectById() {

    }

    @Test
    public void postObject() {

        ResponseEntity<String> response = WebClient.create()
            .post()
            .uri(getUrl("api/ahah"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"productId\":1234, \"label\": \"label\"}")
            .exchange()
            .flatMap(res -> res.toEntity(String.class))
            .block();

        assertEquals(201, response.getStatusCode().value());

        final Document record = Document.parse(response.getBody());
        assertEquals(1234, record.getInteger("productId"));
        assertEquals("label", record.getString("label"));
        Assertions.assertNotNull(record.getString("_id"));
    }

    @Test
    public void putObject() {

        final Document record = Document.parse("{\"productId\":1234, \"label\": \"label\"}");
        final InsertOneResult insertRes = this.mongoTemplate.getCollection("ahah").insertOne(record);
        final String id = Objects.requireNonNull(insertRes.getInsertedId()).asObjectId().getValue().toString();

        ResponseEntity<String> updateResponse = WebClient.create()
                .put()
                .uri(getUrl("api/ahah") + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"productId\":5678, \"label\": \"label5\"}")
                .exchange()
                .flatMap(res -> res.toEntity(String.class))
                .block();

        assertEquals(204, updateResponse.getStatusCode().value());

        final Document doc = Objects.requireNonNull(this.mongoTemplate
            .getCollection("ahah")
            .find()
            .filter(Filters.eq("_id", new ObjectId(id)))
            .limit(1)
            .first());

        assertEquals("label5", doc.getString("label"));
        assertEquals(5678, doc.getInteger("productId"));
    }

    @Test
    public void deleteObject() {

        final Document record = Document.parse("{\"productId\":1234, \"label\": \"label\"}");
        final InsertOneResult insertRes = this.mongoTemplate.getCollection("ahah").insertOne(record);
        final String id = Objects.requireNonNull(insertRes.getInsertedId()).asObjectId().getValue().toString();

        ResponseEntity<String> updateResponse = WebClient.create()
                .delete()
                .uri(getUrl("api/ahah") + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(res -> res.toEntity(String.class))
                .block();

        assertEquals(200, updateResponse.getStatusCode().value());

        final Document doc = this.mongoTemplate
                .getCollection("ahah")
                .find()
                .filter(Filters.eq("_id", new ObjectId(id)))
                .limit(1)
                .first();

        assertNull(doc);
    }
}
