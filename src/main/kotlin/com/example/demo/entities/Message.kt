package com.example.demo.entities

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
@Table(name="messages")
class Message {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private var id: Long = 0
    private var senderName: String = ""
    private var timestamp: LocalDateTime
    @Column(length = 25000)
    private var message: String = ""
    @ElementCollection
    @CollectionTable(name="photos")
    @Column(length = 10000)
    private var photos: List<String> = ArrayList()
    @ElementCollection
    @CollectionTable(name = "reactions", joinColumns = [JoinColumn(name = "message_id")])
    private var reactions: List<Reaction> = ArrayList()

    constructor(){
        this.senderName = ""
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault())
        this.message = ""
        this.photos = ArrayList()
        this.reactions = ArrayList()
    }

    constructor(senderName: String, timestamp: LocalDateTime, message: String, photos: List<String>, reactions: List<Reaction>){
        this.senderName = senderName
        this.timestamp = timestamp
        this.message = message
        this.photos = photos
        this.reactions = reactions
    }

    fun getId(): Long{
        return this.id
    }

    fun getSenderName(): String{
        return this.senderName
    }

    fun getTimestamp(): LocalDateTime{
        return this.timestamp
    }

    fun getMessage(): String{
        return this.message
    }

    fun getPhotos(): List<String>{
        return this.photos
    }
}