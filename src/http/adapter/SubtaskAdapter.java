package http.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import task.Status;
import task.Subtask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubtaskAdapter extends TypeAdapter<Subtask> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
            jsonWriter.name("startTime").value(subtask.getStartTime().format(formatter));
        }
        jsonWriter.name("epicId").value(subtask.getEpicId());
        jsonWriter.endObject();
    }

    @Override
    public Subtask read(JsonReader jsonReader) throws IOException {
        Subtask subtask;
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.get("id") == null) {
            subtask = new Subtask(jsonObject.get("name").getAsString(), jsonObject.get("description").getAsString(),
                    LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter),
                    Duration.ofMinutes(jsonObject.get("duration").getAsLong()), jsonObject.get("epicId").getAsInt());
        } else {
            subtask = new Subtask(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    Status.valueOf(jsonObject.get("status").getAsString()),
                    LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter),
                    Duration.ofMinutes(jsonObject.get("duration").getAsLong()), jsonObject.get("epicId").getAsInt());
        }
        return subtask;
    }
}
