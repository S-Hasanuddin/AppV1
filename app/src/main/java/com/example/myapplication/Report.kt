package com.example.myapplication

data class Report(
    val title: String = "",        // Title of the report
    val description: String = "",  // Description of the report
    val location: String = "",     // Location of the reported deforestation/pollution
    val imageUrl: String = ""      // URL for an image related to the report (if any)
)