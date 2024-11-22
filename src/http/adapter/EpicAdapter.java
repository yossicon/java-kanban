package http.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import task.Epic;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EpicAdapter extends TypeAdapter<Epic> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(JsonWriter jsonWriter, Epic epic) throws IOException {
        jsonWriter.beginObject();
        if (epic.getId() != null) {
            jsonWriter.name("id").value(epic.getId());
        }
        jsonWriter.name("name").value(epic.getName());
        jsonWriter.name("description").value(epic.getDescription());
        jsonWriter.name("status").value(epic.getStatus().toString());

        if (epic.getDuration() == null) {
            jsonWriter.name("duration").nullValue();
        } else {
            jsonWriter.name("duration").value(epic.getDuration().toMinutes());
        }
        if (epic.getStartTime() == null) {
            jsonWriter.name("startTime").nullValue();
        } else {
            jsonWriter.name("startTime").value(epic.getStartTime().format(formatter));
        }
        jsonWriter.endObject();
    }

    @Override
    public Epic read(JsonReader jsonReader) throws IOException {
        Epic epic;
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.get("id") == null) {
            epic = new Epic(jsonObject.get("name").getAsString(), jsonObject.get("description").getAsString());
        } else {
            epic = new Epic(jsonObject.get("id").getAsInt(), jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString());
        }
        return epic;
    }
}
