package com.example.demo

import org.springframework.stereotype.Component
import java.text.Normalizer
import java.util.regex.Pattern

@Component
class StringProcessor {
    fun removeAccent(input: String): String{
        val normalized: String = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern: Pattern = Pattern.compile("\\p{InCOMBINING_DIACRITICAL_MARKS}+")
        return pattern.matcher(normalized).replaceAll("")
    }
}