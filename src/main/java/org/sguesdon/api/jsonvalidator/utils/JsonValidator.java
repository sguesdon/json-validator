package org.sguesdon.api.jsonvalidator.utils;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sguesdon.api.jsonvalidator.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.exception.InvalidSchemaException;

public class JsonValidator {
    public static void validate(ModelDto model, String json) throws InvalidSchemaException {
        try {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(model.getSchema()));
            JSONObject jsonSubject = new JSONObject(new JSONTokener(json));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
        } catch(
                JSONException exception) {
            throw new InvalidSchemaException("json not valid");
        }
    }
}
