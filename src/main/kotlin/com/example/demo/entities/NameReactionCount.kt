package com.example.demo.entities

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

class NameReactionCount{
    var name: String = ""
    var reaction: String = ""
    var count: Int = 0

    constructor(){
        this.name = ""
        this.reaction = ""
        this.count = 0
    }

    constructor(name: String, reaction: String, count: Int){
        this.name = name
        this.reaction = reaction
        this.count = count
    }
}