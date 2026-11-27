# Verbindliche Regeln für GymMane Scanner

Diese Regeln gelten für das gesamte Repository und alle zukünftigen Änderungen.
Ausdrückliche Vorgaben des Nutzers haben Vorrang. Historische Anleitungen zu
lokalen Builds setzen die folgenden Regeln nicht außer Kraft.

## Branches und Commits

- Jede Änderung und jedes Feature in einem separaten, aussagekräftig benannten
  Branch entwickeln. Niemals direkt auf `main` arbeiten, committen oder pushen.
- Vor Änderungen `git status` und den aktuellen Branch prüfen. Fremde oder bereits
  vorhandene Änderungen erhalten und nicht versehentlich mitcommitten.
- Nur zum Auftrag gehörende Dateien gezielt stagen, den vorgemerkten Diff prüfen,
  mit einer aussagekräftigen Commit-Nachricht committen und den Arbeitsbranch pushen.
- Keine eigenständigen Merges in `main`, keine Force-Pushes und kein Umschreiben
  fremder Git-Historie.

## Builds ausschließlich in GitHub Actions

- Keine lokalen APK- oder AAB-Builds durchführen, auch nicht zur Verifikation.
  Keine lokalen Gradle-Aufrufe für `assemble`, `bundle`, `build`, `install` oder
  vergleichbare Tasks starten und keine lokalen Build-Umgebungen dafür einrichten.
- Kompilierung, Unit-Tests, Android Lint und APK-Erstellung über die vorhandenen
  GitHub-Actions-Workflows ausführen. Lokal sind nicht bauende Prüfungen wie
  Diff-Review, `git diff --check` und Ressourcenprüfungen erlaubt.
- `.github/workflows/ci.yml` startet derzeit bei Pushes auf `main`, bei Pull
  Requests gegen `main` und manuell über `workflow_dispatch`. Ein Push auf einen
  Feature-Branch allein startet keinen Build. Für die Prüfung dieses Branches
  den bestehenden CI-Workflow manuell mit diesem Branch starten oder einen
  Pull Request gegen `main` verwenden. Nicht zur CI-Auslösung nach `main` mergen.
- `.github/workflows/release.yml` bleibt der bestehende manuelle Release-Prozess
  auf `main`: unsignierte APK bauen, separat mit den vorhandenen Secrets signieren
  und optional einen Release-Entwurf erstellen. Feature-Arbeit ist keine Freigabe
  zur Release-Veröffentlichung.
- Workflows, Repository-Einstellungen, Secrets und Berechtigungen ohne
  ausdrücklichen Auftrag weder ändern, löschen noch neu anlegen.
- Gradle-Wrapper einschließlich JAR, Builddateien, SDK-/Java-Konfiguration,
  ProGuard-Regeln und die für CI benötigten Abhängigkeiten erhalten.
- Keine lokale Signierung oder automatische lokale APK-Archivierung wieder
  einführen. Keine Zugangsdaten, Keystores, APKs, Build-Ausgaben oder Caches
  committen. Bestehende private Schlüssel und Release-Archive nicht löschen.

## Bestehendes Design und UX beibehalten

- Das vorhandene App-Design ist verbindlich. Ohne ausdrücklichen Designauftrag
  weder Farbschema, Layout, Typografie, Komponenten, Navigation noch UX-Konzept
  verändern. Neue Funktionen optisch und im Verhalten in die vorhandene App
  einfügen; keine eigenständige neue Designsprache einführen.
- `app/src/main/java/de/gymmane/scanner/ui/theme/Theme.kt` ist die Referenz für
  Farben und Typografie: dunkler Hintergrund `#0C0D0C`, Flächen `#1C1D1B`,
  Felder `#292A27`, Apricot `#DCA17F`, Gold `#E1B456`, Sage `#A9BD8D`,
  Text `#F5F4F0` und Nunito. Bestehende Theme-Werte und Textstile wiederverwenden.
- Komponenten aus `ui/Components.kt` wiederverwenden, insbesondere vorhandene
  Buttons, Textfelder, Karten und Auswahlfelder. Rundungen, Abstände,
  Größenverhältnisse, Icon-Stil und visuelle Hierarchie beibehalten.
- Animationen über die bestehenden Hilfen in `ui/Motion.kt` integrieren und
  reduzierte Animationen respektieren. Bedienbarkeit, Touch-Ziele und
  Barrierefreiheit erhalten.
- Bestehende Screens und `screenshots/` als visuelle Referenz heranziehen.
  Scan-, Produkt-, Verlaufs-, Einstellungs-, Onboarding- und Importabläufe
  einschließlich Lade-, Fehler- und Leerzuständen konsistent halten.

## Architektur, Umfang und Abhängigkeiten

- Kotlin, Jetpack Compose, die bestehende Paketstruktur und vorhandene
  Konventionen respektieren. Zuständigkeiten von `data/`, `domain/`,
  `integration/`, `ui/`, `ui/theme/`, `updates/` und `i18n/` beibehalten.
- Bestehende ViewModels, Repositories und Datenmodelle nutzen. Keine parallelen
  Architekturen, unnötigen Abstraktionen oder doppelten Implementierungen anlegen.
- Änderungen auf den jeweiligen Auftrag begrenzen. Keine unnötigen Refactorings,
  Umbenennungen, großflächigen Formatierungen oder beiläufigen Versionsupdates.
- Keine unnötigen Abhängigkeiten hinzufügen. Zuerst vorhandene Bibliotheken und
  Plattformfunktionen nutzen. Keine neuen Google-Dienste oder Google-SDKs wie
  Google Play Services, Firebase oder Google Analytics einführen. Bestehende
  Android-/AndroidX-/Compose- und CI-Abhängigkeiten bleiben erhalten.
- Keine Telemetrie, Analytics, Tracking, Werbe-SDKs oder automatische Übertragung
  von Nutzungs-, Diagnose- oder personenbezogenen Daten hinzufügen. Keine
  zusätzlichen Netzwerkaufrufe oder Berechtigungen ohne fachliche Notwendigkeit.
- Vorhandene deutsche und englische Lokalisierung über Ressourcen und `i18n/T.kt`
  fortführen. Keine neuen fest verdrahteten UI-Texte einführen.

## Prüfung und Abschluss

- Vor jedem Commit den gesamten relevanten Diff auf Fehler, Kompatibilität und
  unbeabsichtigte Seiteneffekte prüfen; `git diff --check` ausführen und den
  vorgemerkten Diff kontrollieren.
- Besonders Android ab API 26, Daten-/Cache-Kompatibilität, Offline-Verhalten,
  GymMane-Import, Updates, Lokalisierung und bestehende UX berücksichtigen.
- Bei Verhaltensänderungen vorhandene passende Tests pflegen oder gezielt
  ergänzen. Tests und Lint online ausführen; keinen lokalen APK-Build als Ersatz
  verwenden. CI-Fehler beheben, bevor die Änderung als erfolgreich geprüft gilt.
- Im Abschluss Änderungen, ausgeführte Prüfungen, CI-Ergebnis und verbleibende
  Einschränkungen wahrheitsgemäß nennen. Nicht ausgeführte Prüfungen ausdrücklich
  kennzeichnen und keine erfolgreiche CI-Prüfung behaupten, wenn sie aussteht.
