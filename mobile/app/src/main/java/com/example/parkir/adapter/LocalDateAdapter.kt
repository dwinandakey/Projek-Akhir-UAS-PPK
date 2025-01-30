package com.example.parkir.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// First, create a custom TypeAdapter for LocalDate
class LocalDateAdapter : TypeAdapter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        val dateStr = reader.nextString()
        return if (dateStr == null) null else LocalDate.parse(dateStr, formatter)
    }
}
