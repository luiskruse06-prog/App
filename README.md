# Bulk Tracker

Private Android-App (Einzelnutzer) für Lean Bulk: grobes Ernährungs-Tracking nach
Handmethode, Rezepte, Schlaf-Disziplin, Krafttraining und Körpergewicht.
Alle Daten bleiben **lokal auf dem Gerät** – kein Account, kein Backend, keine Tracker.

## Stand

| Phase | Inhalt | Stand |
|---|---|---|
| 1 | Setup, Datenmodell, Navigation, Training-Logging inkl. letzter Werte | ✅ fertig, wartet auf Gerätetest |
| 2 | Ernährung (Portionen, Vorlagen, Tag/Woche) + Rezepte (CRUD, Seed) | offen |
| 3 | Körpergewicht + Fortschritts-Charts | offen |
| 4 | Schlaf (manuell, Health Connect, Streak, Erinnerung) | offen |
| 5 | Rezept-Import per URL/Teilen, Vorschläge, Ausschlussfilter | offen |
| 6 | Dashboard, JSON-Backup, Feinschliff | offen |

## Bauen & installieren

Voraussetzung: Android Studio (Ladybug oder neuer) bzw. JDK 17+ und Android SDK 35.

```bash
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # direkt aufs per USB verbundene Handy
./gradlew testDebugUnitTest      # alle Tests (JVM + Robolectric, kein Gerät nötig)
```

Mindestens Android 9 (API 28).

## Phase 1 testen (auf dem Handy)

1. App öffnen → unten **Training** → **Workout starten**.
2. **+ Übung** → Suche z. B. „bank“ → *Bankdrücken (Langhantel)* antippen.
   Eigene Übung: oben rechts **+** oder Suchbegriff eingeben → „… anlegen“.
3. Gewicht (Komma oder Punkt) × Wiederholungen eintragen → **+**. Satz antippen = bearbeiten,
   Kopier-Symbol = duplizieren, Mülleimer = löschen.
4. Weitere Übungen/Sätze, dann oben **Beenden** → Workout erscheint im Verlauf.
5. Neues Workout, gleiche Übung: Unter dem Namen steht **„Letztes Mal (Datum): …“**, die
   Felder sind mit dem passenden Satz der letzten Einheit vorbefüllt.
6. Übungsauswahl: zuletzt genutzte Übungen stehen oben (Uhr-Symbol).

Bekannte Grenzen von Phase 1: Übungen können noch nicht umbenannt/archiviert werden;
Heute, Ernährung, Rezepte und Mehr sind Platzhalter.

## Architektur

```
app/src/main/java/de/luis/bulktracker/
  core/     Formatierung (Dezimalkomma, 24 h), Eingabe-Parsing, Suche
  data/     Room (db/), Repository-Implementierungen (repo/)
  domain/   reine Kotlin-Modelle, Berechnungen (calc/), Repository-Interfaces (repo/)
  di/       Hilt-Module
  ui/       Compose-Screens je Bereich + Navigation
app/schemas/  exportierte Room-Schemas (Grundlage jeder Migration – mit einchecken!)
```

MVVM mit Hilt; UI → ViewModel → Repository-Interface (domain) → Room (data).
`domain/calc` kennt kein Android und ist vollständig per JUnit getestet.
