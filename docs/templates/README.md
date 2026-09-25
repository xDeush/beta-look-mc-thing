# Szablony: mixiny czekajace na nazwy pol

Pliki w tym katalogu maja rozszerzenie `.txt`, zeby **nie trafialy do buildu**.
Kazdy jest gotowy poza jedna rzecza: nazwami pol, ktorych nie dalo sie
ustalic bez zdekompilowanych zrodel 26.2.

## GameRendererMixin.java.txt -- bujanie kamery

Kod jest kompletny. Nie zgadzaja sie cztery nazwy pol w `LocalPlayer`:

| W kodzie | Co to jest |
|---|---|
| `walkDist` | przebyty dystans, narastajaco |
| `walkDistO` | to samo z poprzedniego ticku |
| `bob` | aktualna amplituda bujania |
| `oBob` | amplituda z poprzedniego ticku |

W 26.2 nazywaja sie inaczej. Znajdz je w zdekompilowanym `LocalPlayer`
albo `Entity` (`walkDist` i `walkDistO` historycznie siedza w `Entity`,
`bob`/`oBob` w `Player`), podmien w pliku, zmien rozszerzenie na `.java`,
przenies do `src/client/java/com/betalook/mixin/` i dopisz
`"GameRendererMixin"` do `src/client/resources/betalook.client.mixins.json`.

Sprawdz tez, czy metoda `GameRenderer.bobView` nadal tak sie nazywa --
to string w `@Inject(method = ...)`, wiec kompilator tego nie zlapie,
ale mixin krzyknie przy starcie gry.

## Jak szybko znalezc nazwy

W IntelliJ: `Ctrl+N`, wpisz `LocalPlayer`, Enter. Potem `Ctrl+F12`
pokazuje liste pol.
