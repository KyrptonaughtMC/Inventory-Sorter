package net.kyrptonaught.inventorysorter.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.Reader;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class SchemaValidator {

    public static final String CONFIG_SCHEMA = "config-schema.json";
    public static final String REMOTE_CONFIG_SCHEMA = "config-remote-schema.json";
    public static final String STRING_ARRAY_SCHEMA = "string-array-schema.json";

    public static boolean isValidJsonObject(Reader jsonReader, String schemaResource, String sourceName) {
        return validate(schemaResource, sourceName, () -> {
            JsonElement json = JsonParser.parseReader(jsonReader);
            return new JSONObject(json.toString());
        });
    }

    public static boolean isValidJsonArray(Reader jsonReader, String schemaResource, String sourceName) {
        return validate(schemaResource, sourceName, () -> {
            JsonElement json = JsonParser.parseReader(jsonReader);
            return new JSONArray(json.toString());
        });
    }

    private static boolean validate(String schemaResource, String sourceName, JsonSupplier supplier) {
        try (InputStream schemaStream = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaResource)) {
            if (schemaStream == null) throw new IllegalStateException("Schema not found: " + schemaResource);

            JSONObject schemaObj = new JSONObject(new JSONTokener(schemaStream));
            Schema schema = SchemaLoader.load(schemaObj);

            Object parsedJson = supplier.get();
            schema.validate(parsedJson);
            return true;

        } catch (ValidationException e) {
            LOGGER.error("Validation error in {}:", sourceName);
            LOGGER.error(e.getErrorMessage());
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(LOGGER::error);
            throw new RuntimeException(e);
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax in {}: {}", sourceName, e.getCause().getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Unexpected error validating {}: {}", sourceName, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private interface JsonSupplier {
        Object get() throws Exception;
    }
}
