package http.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import task.Status;
import task.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskAdapter extends TypeAdapter<Task> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        if (task.getId() != null) {
            jsonWriter.name("id").value(task.getId());
        }
        jsonWriter.name("name").value(task.getName());
        jsonWriter.name("description").value(task.getDescription());
        jsonWriter.name("status").value(task.getStatus().toString());

        if (task.getDuration() == null) {
            jsonWriter.name("duration").nullValue();
        } else {
            jsonWriter.name("duration").value(task.getDuration().toMinutes());
        }
        if (task.getStartTime() == null) {
            jsonWriter.name("startTime").nullValue();
        } else {
            jsonWriter.name("startTime").value(task.getStartTime().format(formatter));
        }
        jsonWriter.endObject();
    }

    @Override
    public Task read(JsonReader jsonReader) throws IOException {
        Task task;
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.get("id") == null) {
            if (jsonObject.get("startTime") == null) {
                task = new Task(jsonObject.get("name").getAsString(), jsonObject.get("description").getAsString());
            } else {
                task = new Task(jsonObject.get("name").getAsString(), jsonObject.get("description").getAsString(),
                        LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter),
                        Duration.ofMinutes(jsonObject.get("duration").getAsLong()));
            }
        } else if (jsonObject.get("startTime") == null) {
            task = new Task(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    Status.valueOf(jsonObject.get("status").getAsString()));
        } else {
            task = new Task(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    Status.valueOf(jsonObject.get("status").getAsString()),
                    LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter),
                    Duration.ofMinutes(jsonObject.get("duration").getAsLong()));
        }
        return task;
    }
}
