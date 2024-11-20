package com.example.demo

import com.example.demo.entities.Message
import com.example.demo.entities.Reaction
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class FileService(@Autowired private val resourceLoader: ResourceLoader) {
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun readJsonFile(): ArrayList<JsonNode> {
        val folder = "classpath:/messages/kira"
        val resource: Resource = resourceLoader.getResource(folder)

        val nodeList: ArrayList<JsonNode> = ArrayList()

        resource.file.listFiles()?.forEach { file ->
            if(file.extension == "json")
                nodeList.add(objectMapper.readTree(file))
        }
        return nodeList
    }

    fun extractMessages(nodes: ArrayList<JsonNode>): ArrayList<Message> {
        val messages: ArrayList<Message> = ArrayList()
        nodes.forEach { node ->
            if(isNewMessageFile(node)){
                messages.addAll(parseNewMessageFile(node))
            }else{
                messages.addAll(parseOldMessageFile(node))
            }
        }
        return messages
    }

    private fun parseOldMessageFile(node: JsonNode): ArrayList<Message> {
        val messages: ArrayList<Message> = ArrayList()
        node["messages"].forEach { mes ->
            val reactions: ArrayList<Reaction> = ArrayList()
            if(mes.has("reactions")) {
                mes["reactions"].forEach { r ->
                    reactions.add(
                        Reaction(
                            fixMisencodedString(r["actor"].asText()),
                            fixMisencodedString(r["reaction"].asText())
                        )
                    )
                }
            }
            if (mes.has("content")) {
                messages.add(
                    Message(
                        fixMisencodedString(mes["sender_name"].asText()),
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(mes["timestamp_ms"].asLong()), ZoneId.systemDefault()),
                        fixMisencodedString(mes["content"].asText()),
                        ArrayList(),
                        reactions
                    )
                )
            } else if (mes.has("photos")) {
                val photos: ArrayList<String> = ArrayList()
                mes["photos"].forEach { node ->
                    photos.add(node["backup_uri"].asText())
                }
                messages.add(
                    Message(
                        fixMisencodedString(mes["sender_name"].asText()),
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(mes["timestamp_ms"].asLong()), ZoneId.systemDefault()),
                        "${photos.size} photo(s)",
                        photos,
                        reactions
                    )
                )
            }
        }
        return messages
    }

    private fun parseNewMessageFile(node: JsonNode): ArrayList<Message>{
        //val typeMap: MutableMap<String, Int> = HashMap()
        val messages: ArrayList<Message> = ArrayList()
        val textTypes = arrayOf("link", "text", "placeholder")
        val mediaTypes = arrayOf("media")
        node["messages"].forEach { mes ->
            //typeMap[mes["type"].asText()] = (typeMap[mes["type"].asText()] ?: 0) + 1
            val medias: ArrayList<String> = ArrayList()
            mes["media"].forEach { media ->
                medias.add(media["uri"].asText())
            }
            val reactions: ArrayList<Reaction> = ArrayList()
            if(mes.has("reactions")) {
                mes["reactions"].forEach { r ->
                    reactions.add(
                        Reaction(
                            r["actor"].asText(),
                            r["reaction"].asText()
                        )
                    )
                }
            }
            messages.add(Message(
                mes["senderName"].asText(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(mes["timestamp"].asLong()), ZoneId.systemDefault()),
                if(mes["type"].asText() == "media") "${medias.size} photo(s)" else mes["text"].asText() ,
                medias,
                reactions
            ))
        }
        //log.info(typeMap.toString())
        return messages
    }

    private fun isNewMessageFile(node: JsonNode): Boolean {
        return node.has("threadName")
    }

    private fun fixMisencodedString(s: String): String {
        val isoBytes = s.toByteArray(Charsets.ISO_8859_1)
        return String(isoBytes, Charsets.UTF_8)
    }

    private fun epochToDateTimeString(epoch: Long): String {
        val instant: Instant = Instant.ofEpochMilli(epoch)
        val zoneId: ZoneId = ZoneId.systemDefault()
        val localDateTime: LocalDateTime = instant.atZone(zoneId).toLocalDateTime()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }
}