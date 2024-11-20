package com.example.demo

import com.example.demo.entities.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HelloController(private var messageService: MessageService,
                      @Autowired private val fileService: FileService,
                      @Autowired private val api: ApiController,
                      @Autowired private val stringProcessor: StringProcessor) {
    init {
        /*fileService.extractMessages(fileService.readJsonFile()).forEach { mes ->
            messageService.saveMessage(mes)
        }*/
    }

    var messages = api.allMessages(1)

    @GetMapping("/")
    fun home(model: Model): String{
        model.addAttribute("messages", messages.data.filterIsInstance<Message>()
            .sortedBy { message -> message.getTimestamp() })
        return "message"
    }

    @GetMapping("/stats")
    fun stats(model: Model): String{
        model.addAttribute("totalMessage", api.messageCount())
        model.addAttribute("messagePerDay", api.messagePerDay())
        model.addAttribute("messagePerDayExclude", api.messagePerDay("exclude"))
        model.addAttribute("participants", messageService.getParticipantsWithCount())
        model.addAttribute("startDate", api.startDate())
        model.addAttribute("endDate", api.endDate())
        return "stats"
    }

    @GetMapping("/stats/dates")
    fun dates(model: Model): String{
        model.addAttribute("messageByDay", api.messageByDay())
        model.addAttribute("startDate", api.startDate())
        model.addAttribute("endDate", api.endDate())
        return "dates"
    }

    @GetMapping("/stats/words")
    fun words(model: Model): String{
        return "words"
    }

    @GetMapping("/stats/reactions")
    fun reactions(model: Model): String{
        model.addAttribute("givenReactions", messageService.getParticipantsGivenReactionCount())
        model.addAttribute("gotReactions", messageService.getParticipantsGotReactionCount())
        model.addAttribute("givenReactionPerson", messageService.getGivenReactionCountWithNames())
        model.addAttribute("gotReactionPerson", messageService.getGotReactionCountWithNames())
        return "reactions"
    }
}