#!/usr/bin/env bash
# Testy matematyki bety. Nie wymagaja Minecrafta, Gradle ani sieci --
# klasy w client/{light,fog,color} celowo nie zaleza od klas gry.
set -euo pipefail
cd "$(dirname "$0")/.."

OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT

# Wymieniamy pliki po nazwie, a nie cale katalogi: testujemy wylacznie klasy
# bez zaleznosci od Minecrafta. LightmapWriter siedzi obok BetaLightmap,
# ale uzywa loggera moda, wiec tutaj nie nalezy.
BASE=src/client/java/com/betalook/client
javac -d "$OUT" \
  "$BASE/light/BetaLightmap.java" \
  "$BASE/fog/BetaFog.java" \
  "$BASE/color/BetaColors.java" \
  tools/test/BetaMathTest.java

java -cp "$OUT" BetaMathTest
