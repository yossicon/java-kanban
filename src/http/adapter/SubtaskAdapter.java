package http.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import manager.DateTimeUtil;
import task.Status;
import task.Subtask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskAdapter extends TypeAdapter<Subtask> {

    @Override
    public void write(JsonWriter jsonWriter, Subtask subtask) throws IOException {
        jsonWriter.beginObject();
        if (subtask.getId() != null) {
            jsonWriter.name("id").value(subtask.getId());
        }
        jsonWriter.name("name").value(subtask.getName());
        jsonWriter.name("description").value(subtask.getDescription());
        jsonWriter.name("status").value(subtask.getStatus().toString());
        if (subtask.getDuration() == null) {
            jsonWriter.name("duration").nullValue();
        } else {
            jsonWriter.name("duration").value(subtask.getDuration().toMinutes());
        }
        if (subtask.getStartTime() == null) {
            jsonWriter.name("startTime").nullValue();
        } else {
            jsonWriter.name("startTime").value(subtask.getStartTime().format(DateTimeUtil.FORMATTER));
        }
        jsonWriter.name("epicId").value(subtask.getEpicId());
        jsonWriter.endObject();
    }

    @Override
    public Subtask read(JsonReader jsonReader) {
        Subtask subtask;
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.get("id") == null) {
            subtask = new Subtask(jsonObject.get("name").getAsString(), jsonObject.get("description").getAsString(),
                    LocalDateTime.parse(jsonObject.get("startTime").getAsString(), DateTimeUtil.FORMATTER),
                    Duration.ofMinutes(jsonObject.get("duration").getAsLong()), jsonObject.get("epicId").getAsInt());
        } else {
            subtask = new Subtask(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    Status.valueOf(jsonObject.get("status").getAsString()),
                    LocalDateTime.parse(jsonObject.get("startTime").getAsString(), DateTimeUtil.FORMATTER),
                    Duration.ofMinutes(jsonObject.get("duration").getAsLong()), jsonObject.get("epicId").getAsInt());
        }
        return subtask;
    }
}
