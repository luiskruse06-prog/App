package de.luis.bulktracker.domain.model

/*
 * Aufzählungen werden von Room über ihren NAMEN gespeichert. Einen Namen umbenennen
 * heißt deshalb: Migration schreiben. Die deutschen Anzeigetexte stehen in [label]
 * und dürfen sich jederzeit ändern.
 */

enum class MuscleGroup(val label: String) {
    BRUST("Brust"),
    RUECKEN("Rücken"),
    SCHULTERN("Schultern"),
    BIZEPS("Bizeps"),
    TRIZEPS("Trizeps"),
    BEINE("Beine"),
    PO("Po"),
    WADEN("Waden"),
    BAUCH("Bauch"),
    GANZKOERPER("Ganzkörper"),
    SONSTIGES("Sonstiges"),
}

enum class MealSlot(val label: String) {
    FRUEHSTUECK("Frühstück"),
    MITTAG("Mittag"),
    ABEND("Abendessen"),
    SNACK("Snack"),
}

enum class RecipeCategory(val label: String) {
    FRUEHSTUECK("Frühstück"),
    ABENDESSEN("Abendessen"),
    SNACK("Snack"),
}

enum class Appliance(val label: String) {
    HERD("Herd"),
    OFEN("Ofen"),
    AIRFRYER("Airfryer"),
    MIKROWELLE("Mikrowelle"),
    SANDWICHMAKER("Sandwichmaker"),
}

enum class SleepSource {
    MANUAL,
    HEALTH_CONNECT,
}
