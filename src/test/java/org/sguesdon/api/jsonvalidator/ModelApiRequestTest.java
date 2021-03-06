package org.sguesdon.api.jsonvalidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sguesdon.api.jsonvalidator.openapi.model.Model;
import org.sguesdon.api.jsonvalidator.repository.ModelRepository;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ModelApiRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelRepository repository;

    private String getUrl(String uri) {
        return "http://localhost:" + port + "/" + uri;
    }

    @BeforeEach
    public void beforeEach() {
        this.repository.deleteAll();
    }

    @Test
    public void postModel() throws Exception {

        final Model model = new Model();
        model.setName("name");
        model.setTag("tag1,tag2");
        model.setCollection("collection");
        model.setEndpoint("tag1,tag2");
        model.setSchema("{}");

        Model response = WebClient.create()
            .post()
            .uri(getUrl("models"))
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(model), Model.class)
            .retrieve()
            .bodyToMono(Model.class)
            .block();

        JSONAssert.assertEquals(
            "{id:" + response.getId() + ",name:\"name\",tag: \"tag1,tag2\"}",
            this.objectMapper.writeValueAsString(response),
            JSONCompareMode.LENIENT
        );
    }

    @Test
    public void postInvalidSchemaModel() throws Exception {

        final Model model = new Model();
        model.setName("name");
        model.setTag("tag1,tag2");
        model.setCollection("collection");
        model.setEndpoint("endpoint");
        model.setSchema("{");

        String response = WebClient.create()
                .post()
                .uri(getUrl("models"), "not_exist")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), Model.class)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(ex.getResponseBodyAsString()))
                .block();

        Assertions.assertEquals(
                "{\"code\":400,\"message\":\"invalid json schema\",\"trace\":\"\"}",
                response
        );
    }

    @Test
    public void postAndPutModel() throws Exception {

        final Model model = new Model();
        model.setName("initial_name");
        model.setTag("initial_tag");
        model.setCollection("initial_collection");
        model.setEndpoint("initial_endpoint");
        model.setSchema("{}");

        Model response = WebClient.create()
                .post()
                .uri(getUrl("models"))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        response.setName("new_name");
        response.setTag("new_tag");

        response = WebClient.create()
                .put()
                .uri(getUrl("models/{id}"), response.getId())
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(response), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        JSONAssert.assertEquals(
                "{id:" + response.getId() + ",name:\"new_name\",tag: \"new_tag\"}",
                this.objectMapper.writeValueAsString(response),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void putMissingModel() throws Exception {

        final Model model = new Model();
        model.setName("initial_name");
        model.setTag("initial_tag");
        model.setCollection("initial_collection");
        model.setEndpoint("initial_endpoint");
        model.setSchema("{}");

        String response = WebClient.create()
                .put()
                .uri(getUrl("models/{id}"), "not_exist")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), Model.class)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(ex.getResponseBodyAsString()))
                .block();

        Assertions.assertEquals(
                "{\"code\":404,\"message\":\"model with id not_exist doesn't exist\",\"trace\":\"\"}",
                response
        );
    }

    @Test
    public void postAndDeleteModel() throws Exception {

        final Model model = new Model();
        model.setName("initial_name");
        model.setTag("initial_tag");
        model.setCollection("initial_collection");
        model.setEndpoint("initial_endpoint");
        model.setSchema("{}");

        Model response = WebClient.create()
            .post()
            .uri(getUrl("models"))
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(model), Model.class)
            .retrieve()
            .bodyToMono(Model.class)
            .block();

        WebClient.create()
            .delete()
            .uri(getUrl("models/{id}"), response.getId())
            .retrieve()
            .bodyToMono(Model.class)
            .block();

        ResponseEntity<Void> getResponse = WebClient.create()
                .get()
                .uri(getUrl("models/{id}"), response.getId())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (r) -> Mono.empty())
                .toBodilessEntity()
                .block();

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void postAndGetByIdModel() throws JsonProcessingException, JSONException {

        final Model model = new Model();
        model.setName("post_get");
        model.setTag("post_get");
        model.setCollection("post_collection");
        model.setEndpoint("post_endpoint");
        model.setSchema("{}");

        Model response = WebClient.create()
                .post()
                .uri(getUrl("models"))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        response = WebClient.create()
                .get()
                .uri(getUrl("models/{id}"), response.getId())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        JSONAssert.assertEquals(
                "{id:" + response.getId() + ",name:\"post_get\",tag: \"post_get\"}",
                this.objectMapper.writeValueAsString(response),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void postAndGetPaginateModels() {

        final Model model = new Model();
        model.setTag("tag");
        model.setSchema("{}");

        for(int i = 0; i < 50; i++) {

            model.setName("name_" + i);
            model.setCollection("collection_" + i);
            model.setEndpoint("endpoint_" + i);

            WebClient.create()
                .post()
                .uri(getUrl("models"))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();
        }

        int num = 0;
        int page = 0;
        final int size = 10;
        List<Model> items = null;

        while(items == null || items.size() > 0) {
            int finalPage = page;
            items = WebClient.create(getUrl(""))
                .get()
                .uri(
                    uriBuilder -> uriBuilder
                        .path("models")
                        .queryParam("size", size)
                        .queryParam("page", finalPage)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Model.class)
                .collectList()
                .block();
            num += items.size();
            page++;
        }

        assertThat(num).isEqualTo(50);
    }

    @Test
    public void postAndValidateJson() throws IOException, JSONException {

        final Model data = new Model();
        data.setName("post_get");
        data.setTag("post_get");
        data.setCollection("ahah");
        data.setEndpoint("ahah");
        data.setSchema(Files.readString(Path.of("src/test/resources/json_schema_1.json")));

        Model model = WebClient.create()
                .post()
                .uri(getUrl("models"))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(data), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        ResponseEntity<Void> response = WebClient.create()
                .post()
                .uri(getUrl("models/{id}/validate"), model.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Files.readString(Path.of("src/test/resources/json_data_1.json")))
                .retrieve()
                .toBodilessEntity()
                .block();

        Assertions.assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void postAndFailedValidateJson() throws IOException, JSONException {

        final Model data = new Model();
        data.setName("name");
        data.setTag("tag");
        data.setCollection("collection");
        data.setEndpoint("endpoint");
        data.setSchema(Files.readString(Path.of("src/test/resources/json_schema_1.json")));

        Model model = WebClient.create()
                .post()
                .uri(getUrl("models"))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(data), Model.class)
                .retrieve()
                .bodyToMono(Model.class)
                .block();

        String response = WebClient.create()
                .post()
                .uri(getUrl("models/{id}/validate"), model.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(ex.getResponseBodyAsString()))
                .block();

        Assertions.assertEquals("{\"code\":400,\"message\":\"invalid json : #: required key [productId] not found\",\"trace\":\"\"}", response);
    }
}
