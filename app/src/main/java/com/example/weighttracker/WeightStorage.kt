package com.example.weighttracker

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File

class WeightStorage(context: Context) {

    private val file = File(context.filesDir, "weights.xml")

    fun loadEntries(): List<WeightEntry> {
        if (!file.exists()) return emptyList()

        val entries = mutableListOf<WeightEntry>()

        try {
            val parser = Xml.newPullParser()
            parser.setInput(file.inputStream(), "UTF-8")

            var tag = parser.eventType
            while (tag != XmlPullParser.END_DOCUMENT) {
                if (tag == XmlPullParser.START_TAG && parser.name == "entry") {
                    entries.add(
                        WeightEntry(
                            id        = parser.getAttributeValue(null, "id").toInt(),
                            weight    = parser.getAttributeValue(null, "weight").toDouble(),
                            timestamp = parser.getAttributeValue(null, "timestamp").toLong()
                        )
                    )
                }
                tag = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return entries
    }

    fun saveEntries(entries: List<WeightEntry>) {
        try {
            val serializer = Xml.newSerializer()
            val writer = file.writer()

            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.startTag(null, "weights")

            for (entry in entries) {
                serializer.startTag(null, "entry")
                serializer.attribute(null, "id",        entry.id.toString())
                serializer.attribute(null, "weight",    entry.weight.toString())
                serializer.attribute(null, "timestamp", entry.timestamp.toString())
                serializer.endTag(null, "entry")
            }

            serializer.endTag(null, "weights")
            serializer.endDocument()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
