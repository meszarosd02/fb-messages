package com.example.demo

import com.example.demo.entities.Message
import com.example.demo.entities.Paginated
import com.example.demo.entities.ParticipantWithCount
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.collections.HashMap
import kotlin.math.min

@RestController
@RequestMapping("/api")
class ApiController(private val messageService: MessageService) {
    private val PER_PAGE = 50

    @GetMapping("/search")
    fun search(@RequestParam message: String): List<Message> {
        return messageService.getMessagesByText(message).sortedBy { mes -> mes.getTimestamp() }
    }

    @GetMapping("/messages")
    fun allMessages(@RequestParam page: Int): Paginated {
        val messages = messageService.getAllMessages()
        val paginated = Paginated(
            paginateList(messages, page).filterIsInstance<Message>().sortedBy { mes -> mes.getTimestamp() },
            page,
            PER_PAGE,
            messages.size / PER_PAGE + 1
        )
        return paginated
    }

    @GetMapping("/messages/count")
    fun messageCount(): Int {
        return messageService.getMessageCount()
    }

    @GetMapping("/messages/per_day")
    fun messagePerDay(@RequestParam(defaultValue = "include") mode: String = "include"): Int {
        return when(mode){
            "include" -> {
                val first = messageService.getFirstMessage().getTimestamp()
                val last = messageService.getLastMessage().getTimestamp()
                messageCount() / first.until(last, ChronoUnit.DAYS).toInt()
            }
            "exclude" -> {
                val messages = messageByDayRange(startDate(), endDate())
                val days = messages.filterValues { count ->
                    count > 0
                }.size
                messageCount() / days
            }
            else -> 0
        }
    }

    @GetMapping("/messages/start_date")
    fun startDate(): String {
        val toPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val first = messageService.getFirstMessage().getTimestamp()
        return first.atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().format(toPattern)
    }

    @GetMapping("/messages/end_date")
    fun endDate(): String {
        val toPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val last = messageService.getLastMessage().getTimestamp()
        return last.atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().format(toPattern)
    }

    @GetMapping("/messages/by_day")
    fun messageByDay(): Map<String, Int> {
        val map: MutableMap<String, Int> = messageService.getMessageCountByDay()
        val toPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val first = messageService.getFirstMessage().getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate()
        val last = messageService.getLastMessage().getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate()
        val dateRange = DateRange(first, last, 1)
        dateRange.forEach { date ->
            val day = date.format(toPattern)
            if(!map.containsKey(day)) {
                map[day] = 0
            }
        }
        return map.toSortedMap()
    }

    @GetMapping("/messages/by_day_range")
    fun messageByDayRange(@RequestParam start: String, @RequestParam end: String): Map<String, Int> {
        val map: MutableMap<String, Int> = messageService.getMessageCountByDateRange(
            LocalDate.parse(start).atStartOfDay(),
            LocalDate.parse(end).atStartOfDay()
        )
        val toPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(start, toPattern)
        val endDate = LocalDate.parse(end, toPattern)
        val dateRange = DateRange(startDate, endDate)
        dateRange.forEach { date ->
            val day = date.format(toPattern)
            if(!map.containsKey(day)) {
                map[day] = 0
            }
        }
        return map.toSortedMap()
    }

    @GetMapping("/messages/by_date")
    fun getMessageByDate(@RequestParam date: String, @RequestParam page: Int): Paginated {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val epoch = LocalDate.parse(date, pattern).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val list = messageService.getMessageByDate(epoch)
        val paginated = Paginated(
            paginateList(list, page).filterIsInstance<Message>().sortedBy { mes -> mes.getTimestamp() },
            page,
            PER_PAGE,
            list.size / PER_PAGE + 1
        )
        return paginated
    }

    @GetMapping("/messages/participants")
    fun getParticipants(): List<ParticipantWithCount>{
        return messageService.getParticipantsWithCount()
    }

    @GetMapping("/messages/by_persons")
    fun getMessagesByPerson(): Map<String, List<Message>>{
        val participantsName = getParticipants().map { p -> p.name }
        val list = participantsName.associate { name ->
            name to messageService.getMessagesBySenderName(name)
        }
        return list
    }

    private fun paginateList(list: List<Any>, page: Int): List<Any>{
        return list.subList((page - 1) * PER_PAGE, min((page) * PER_PAGE - 1, list.size))
    }

    class DateRange(
        override val start: LocalDate,
        override val endInclusive: LocalDate,
        val step: Long = 1
    ) : Iterable<LocalDate>, ClosedRange<LocalDate> {
        override fun iterator(): Iterator<LocalDate> {
            return DateIterator(start, endInclusive, step)
        }

        infix fun step(days: Long) = DateRange(start, endInclusive, step)

    }

    class DateIterator(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val step: Long
    ) : Iterator<LocalDate> {
        private var currentDate = startDate
        override fun hasNext(): Boolean {
            return currentDate.plusDays(step) <= endDate
        }

        override fun next(): LocalDate {
            val next = currentDate.plusDays(step)
            currentDate = next
            return next
        }

    }
}