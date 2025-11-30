package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(formatter.format(localDateTime));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        switch (jsonReader.peek()) {
            case NULL:
                jsonReader.nextNull();
                return null;
            default:
                String date = jsonReader.nextString();
                return LocalDateTime.parse(date, formatter);
        }
    }
}

