package com.example.demo.entities

class ParticipantWithCount{
    var name: String = ""
    var count: Long   = 0

    constructor(){
        this.name = ""
        this.count = 0
    }
    constructor(name: String, count: Long){
        this.name = name
        this.count = count
    }
}