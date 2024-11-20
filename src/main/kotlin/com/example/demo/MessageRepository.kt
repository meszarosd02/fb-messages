package com.example.demo

import com.example.demo.entities.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MessageRepository: JpaRepository<Message, Long> {
    fun findBySenderName(senderName: String): ArrayList<Message>

    @Query(value = "SELECT * FROM messages WHERE message LIKE CONCAT('%', :content, '%')", nativeQuery = true)
    fun findByText(@Param("content") message: String): ArrayList<Message>

    @Query(value = "select * from messages where timestamp between :start and :end", nativeQuery = true)
    fun findBetweenDates(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): ArrayList<Message>

    @Query(value = "select * from messages where timestamp between :datum and :datum + interval 1 day", nativeQuery = true)
    fun findByDate(@Param("datum") datum: LocalDateTime): ArrayList<Message>

    @Query(value = "select m.sender_name, count(*) c from messages m group by m.sender_name order by c desc", nativeQuery = true)
    fun getParticipantsWithCount(): List<Array<Any>> //<String, Long>

    @Query(value = "select r.name as name, count(*) from messages m inner join reactions r on r.message_id = m.id group by name", nativeQuery = true)
    fun getParticipantsGivenReactionCount(): List<Array<Any>> //<String, Long>

    @Query(value = "select m.sender_name as name, count(*) from messages m inner join reactions r on r.message_id = m.id group by name", nativeQuery = true)
    fun getParticipantsGotReactionCount(): List<Array<Any>> //<String, Long>

    @Query(value = "select sender_name from messages group by sender_name", nativeQuery = true)
    fun getParticipantList(): List<String>

    @Query(value = "select count(*) from messages", nativeQuery = true)
    fun getMessageCount(): Int

    @Query(value = "select * from messages m order by m.timestamp asc limit 1", nativeQuery = true)
    fun getFirstMessage(): Message

    @Query(value = "select * from messages m order by m.timestamp desc limit 1", nativeQuery = true)
    fun getLastMessage(): Message

    @Query(value = "select date(m.timestamp) t, count(*) from messages m group by t order by m.timestamp asc", nativeQuery = true)
    fun getMessagesByDay(): List<Array<Any>> //<String, Int>

    @Query(value = "select date(m.timestamp) as t, count(*) from messages m where m.timestamp between :start and :end group by t", nativeQuery = true)
    fun getMessageCountByDateRange(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<Array<Any>> //<String, Int>

    @Query(value = "select r.name, r.reaction, count(*) c from messages m inner join reactions r on m.id = r.message_id group by r.name, hex(r.reaction)", nativeQuery = true)
    fun getGivenReactionCountWithNames(): List<Array<Any>>

    @Query(value = "select m.sender_name, r.reaction, count(*) c from messages m inner join reactions r on m.id = r.message_id group by m.sender_name, hex(r.reaction)", nativeQuery = true)
    fun getGotReactionCountWithNames(): List<Array<Any>>
}