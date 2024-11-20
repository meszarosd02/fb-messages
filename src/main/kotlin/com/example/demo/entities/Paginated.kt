package com.example.demo.entities

data class Paginated(
    var data: List<Any>,
    var currentPage: Int,
    var perPage: Int,
    var totalPages: Int
)
