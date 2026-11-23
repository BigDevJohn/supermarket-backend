package jv.supermarket.audit;

import com.fasterxml.jackson.databind.JsonNode;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.StdSerializer;

public class Jackson2JsonNodeSerializer extends StdSerializer<JsonNode> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    public Jackson2JsonNodeSerializer() {
        super(JsonNode.class);
    }

    @Override
    public void serialize(JsonNode value, JsonGenerator generator, SerializationContext context) {
        context.writeTree(generator, JSON_MAPPER.readTree(value.toString()));
    }
}
