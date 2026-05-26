package de.arturo.bartab.data.model

object SampleData {
    val categories = listOf(
        Category("beer", "Bier", 0),
        Category("soft", "Softdrinks", 1),
        Category("shots", "Shots", 2),
    )

    val products = listOf(
        Product("helles", "Helles", 400, "beer", quickAccess = true, sortOrder = 0),
        Product("weissbier", "Weißbier", 450, "beer", quickAccess = true, sortOrder = 1),
        Product("radler", "Radler", 400, "beer", quickAccess = true, sortOrder = 2),
        Product("wasser", "Wasser", 250, "soft", quickAccess = true, sortOrder = 0),
        Product("cola", "Cola", 300, "soft", quickAccess = true, sortOrder = 1),
        Product("spezi", "Spezi", 320, "soft", quickAccess = true, sortOrder = 2),
        Product("jager", "Jägermeister", 250, "shots", quickAccess = true, sortOrder = 0),
        Product("vodka", "Vodka Shot", 250, "shots", quickAccess = true, sortOrder = 1),
    )
}
