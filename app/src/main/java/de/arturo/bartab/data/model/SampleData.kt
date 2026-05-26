package de.arturo.bartab.data.model

object SampleData {
    val categories = listOf(
        Category("beer", "Bier", 0),
        Category("soft", "Softdrinks", 1),
        Category("shots", "Shots", 2),
    )

    val products = listOf(
        Product("helles", "Helles", 400, "beer", sortOrder = 0),
        Product("weissbier", "Weißbier", 450, "beer", sortOrder = 1),
        Product("radler", "Radler", 400, "beer", sortOrder = 2),
        Product("wasser", "Wasser", 250, "soft", sortOrder = 0),
        Product("cola", "Cola", 300, "soft", sortOrder = 1),
        Product("spezi", "Spezi", 320, "soft", sortOrder = 2),
        Product("jager", "Jägermeister", 250, "shots", sortOrder = 0),
        Product("vodka", "Vodka Shot", 250, "shots", sortOrder = 1),
    )

    val recentSales = listOf(
        "19:12 · 12,50 € · abgeschlossen",
        "19:18 · 8,00 € · abgeschlossen",
        "19:24 · 16,00 € · storniert",
    )
}
