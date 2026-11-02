# CLAUDE.md – Bulk Tracker (Android)

## Projekt
Private Android-App für einen Nutzer: Ernährung grob (Handmethode), Rezepte, Schlaf,
Training, Gewicht. Spezifikation und Phasenplan: README.md (Stand-Tabelle pflegen).

## Harte Regeln
- **Nur lokal.** Kein Backend, keine Analytics/Tracker, kein Account. Netzwerk nur für den
  Rezept-Import (Phase 5). Keine Berechtigung ohne konkreten Bedarf.
- **Keine Datenverluste.** Kein `fallbackToDestructiveMigration`. Jede Schemaänderung:
  `AppDatabase.VERSION` erhöhen, Schema-JSON unter `app/schemas/` einchecken, Migration in
  `Migrations.ALL` + Test. `MigrationTest` erzwingt das.
- Schichten: `ui` → ViewModel → `domain/repo`-Interface → `data`. `domain` importiert nichts
  aus Android/Room. Berechnungen gehören nach `domain/calc` und bekommen Unit-Tests.
- Zeit über injizierte `java.time.Clock`, nie `Instant.now()` in Repositories.

## Sprache & Format
UI, Kommentare, Commits auf Deutsch. Bezeichner Englisch. Dezimalkomma, 24 h, metrisch –
immer über `core/Format` und `core/Parse`.

## Tests
`./gradlew testDebugUnitTest` vor jedem Commit grün (JVM + Robolectric, kein Gerät).
Was nur auf dem Handy prüfbar ist (Tastatur, Health Connect, Benachrichtigungen, Optik),
als „wartet auf Gerätetest“ melden, nie als fertig.

## Commits
Kleine Commits, deutsche Betreffzeile mit der Wirkung, Rumpf mit dem Warum.
