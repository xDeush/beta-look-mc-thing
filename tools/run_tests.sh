#!/usr/bin/env bash
# Testy matematyki bety. Nie wymagaja Minecrafta, Gradle ani sieci --
# klasy w client/{light,fog,color} celowo nie zaleza od klas gry.
set -euo pipefail
cd "$(dirname "$0")/.."

OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT

javac -d "$OUT" \
  $(find src/client/java/com/betalook/client/light \
         src/client/java/com/betalook/client/fog \
         src/client/java/com/betalook/client/color -name '*.java') \
  tools/test/BetaMathTest.java

java -cp "$OUT" BetaMathTest
