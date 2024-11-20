package com.example.demo

import com.example.demo.entities.Message
import com.example.demo.entities.NameReactionCount
import com.example.demo.entities.ParticipantWithCount
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class MessageService(private val messageRepository: MessageRepository) {
    fun saveMessage(message: Message): Message{
        return messageRepository.save(message)
    }

    fun getMessageCount(): Int{
        return messageRepository.getMessageCount()
    }

    fun getAllMessages(): List<Message> {
        return messageRepository.findAll()
    }

    fun getMessagesBySenderName(name: String): List<Message>{
        return messageRepository.findBySenderName(name)
    }

    fun getMessagesByText(text: String): List<Message>{
        return messageRepository.findByText(text)
    }

    fun getMessagesByDateRange(start: Instant, end: Instant): List<Message>{
        return messageRepository.findBetweenDates(LocalDateTime.ofInstant(start, ZoneId.systemDefault()),
            LocalDateTime.ofInstant(end, ZoneId.systemDefault()))
    }

    fun getMessageCountByDateRange(start: LocalDateTime, end: LocalDateTime): MutableMap<String, Int>{
        val map: MutableMap<String, Int> = HashMap()
        val result = messageRepository.getMessageCountByDateRange(start, end)
        result.forEach { obj ->
            map[(obj[0] as Date).toString()] = (obj[1] as Number).toInt()
        }
        return map
    }

    fun getMessageByDate(date: Instant): List<Message>{
        return messageRepository.findByDate(LocalDateTime.ofInstant(date, ZoneId.systemDefault())).sortedBy { mes -> mes.getTimestamp() }
    }

    fun getMessageCountByDay(): MutableMap<String, Int>{
        val map: MutableMap<String, Int> = HashMap()
        val result = messageRepository.getMessagesByDay()
        result.forEach { obj ->
            map[(obj[0] as Date).toString()] = (obj[1] as Number).toInt()
        }
        return map
    }

    fun getParticipantsWithCount(): List<ParticipantWithCount>{
        val result = messageRepository.getParticipantsWithCount()
        return result.map{
            ParticipantWithCount(
                it[0] as String,
                (it[1] as Number).toLong()
            )
        }
    }

    fun getFirstMessage(): Message{
        return messageRepository.getFirstMessage()
    }
    fun getLastMessage(): Message{
        return messageRepository.getLastMessage()
    }

    fun getParticipantsGivenReactionCount(): List<ParticipantWithCount>{
        val result = messageRepository.getParticipantsGivenReactionCount()
        val map: MutableMap<String, Long> = HashMap()
        val participants = messageRepository.getParticipantList()
        participants.forEach { name ->
            map[name] = 0
        }
        result.forEach {
            map[it[0].toString()] = (it[1] as Number).toLong()
        }
        return map.map {
            ParticipantWithCount(
                it.key,
                it.value
            )
        }
    }

    fun getParticipantsGotReactionCount(): List<ParticipantWithCount>{
        val result = messageRepository.getParticipantsGotReactionCount()
        val map: MutableMap<String, Long> = HashMap()
        val participants = messageRepository.getParticipantList()
        participants.forEach { name ->
            map[name] = 0
        }
        result.forEach {
            map[it[0].toString()] = (it[1] as Number).toLong()
        }
        return map.map {
            ParticipantWithCount(
                it.key,
                it.value
            )
        }
    }

    fun getGivenReactionCountWithNames(): Map<String, Map<String, Int>>{
        val map: MutableMap<String, MutableMap<String, Int>> = HashMap()
        val result = messageRepository.getGivenReactionCountWithNames().map {
            NameReactionCount(
                it[0] as String,
                it[1] as String,
                (it[2] as Number).toInt()
            )
        }
        val participants = messageRepository.getParticipantList()
        participants.forEach { name ->
            map[name] = HashMap()
        }

        result.forEach { obj ->
            map[obj.name]!![obj.reaction] = obj.count
        }

        map.forEach { (t, u) ->
            map[t] = u.toList().sortedByDescending { (_, value) -> value}.toMap() as MutableMap<String, Int>
        }

        return map
    }

    fun getGotReactionCountWithNames(): Map<String, Map<String, Int>>{
        val map: MutableMap<String, MutableMap<String, Int>> = HashMap()
        val result = messageRepository.getGotReactionCountWithNames().map {
            NameReactionCount(
                it[0] as String,
                it[1] as String,
                (it[2] as Number).toInt()
            )
        }
        val participants = messageRepository.getParticipantList()
        participants.forEach { name ->
            map[name] = HashMap()
        }

        result.forEach { obj ->
            map[obj.name]!![obj.reaction] = obj.count
        }

        map.forEach { (t, u) ->
            map[t] = u.toList().sortedByDescending { (_, value) -> value}.toMap() as MutableMap<String, Int>
        }

        return map
    }
}