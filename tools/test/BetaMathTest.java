import com.betalook.client.color.BetaColors;
import com.betalook.client.fog.BetaFog;
import com.betalook.client.light.BetaLightmap;
import com.betalook.client.sky.BetaSky;

/**
 * Testy matematyki bety. Nie wymagaja Minecrafta ani Gradle.
 *
 *   javac -d /tmp/bm $(find src/client/java/com/betalook/client/{light,fog,color} -name '*.java') \
 *         tools/test/BetaMathTest.java && java -cp /tmp/bm BetaMathTest
 */
public class BetaMathTest {
    private static int failures = 0;

    public static void main(String[] args) {
        testBrightnessCurve();
        testTorchIsWarm();
        testAmbientFloor();
        testFogStartsAtQuarter();
        testVoidFogDarkens();
        testColorIndex();
        testSkyColor();

        if (failures > 0) {
            System.out.println("\nNIEUDANE: " + failures);
            System.exit(1);
        }
        System.out.println("\nWszystko przeszlo.");
    }

    /** Krzywa jasnosci bety: 0 -> 0.05, 15 -> 1.0, rosnaca. */
    private static void testBrightnessCurve() {
        check("brightness(0) == AMBIENT",
                Math.abs(BetaLightmap.brightness(0) - BetaLightmap.AMBIENT) < 1e-6);
        check("brightness(15) == 1.0",
                Math.abs(BetaLightmap.brightness(15) - 1.0F) < 1e-5);

        boolean rising = true;
        for (int i = 1; i < 16; i++) {
            if (BetaLightmap.brightness(i) <= BetaLightmap.brightness(i - 1)) {
                rising = false;
            }
        }
        check("krzywa jasnosci jest rosnaca", rising);
    }

    /**
     * Sedno beta-looku: swiatlo pochodni musi byc CIEPLE, czyli R > G > B.
     *
     * Mierzymy przy poziomie 8, nie 15. Tuz przy pochodni wszystkie trzy kanaly
     * i tak dobijaja do 1.0 i plama jest biala -- tak samo bylo w becie.
     * Pomaranczowa barwa pojawia sie dopiero w polowie zasiegu swiatla, gdzie
     * kanaly G i B opadaja szybciej niz R. Test przy poziomie 15 mierzylby
     * wylacznie nasycenie i przechodzilby dla dowolnego wzoru.
     */
    private static void testTorchIsWarm() {
        for (int level : new int[] {6, 8, 10}) {
            int argb = BetaLightmap.color(0, level, 0.0F, 0.0F, 0.0F, false);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            check("poziom " + level + ": R > G (R=" + r + " G=" + g + ")", r > g);
            check("poziom " + level + ": G > B (G=" + g + " B=" + b + ")", g > b);
        }

        int near = BetaLightmap.color(0, 15, 0.0F, 0.0F, 0.0F, false);
        check("tuz przy zrodle kanaly sa nasycone (jak w becie)",
                ((near >> 16) & 0xFF) == 255 && (near & 0xFF) == 255);
    }

    /** W becie nic nie bylo absolutnie czarne. */
    private static void testAmbientFloor() {
        int argb = BetaLightmap.color(0, 0, 0.0F, 0.0F, 0.0F, false);
        int r = (argb >> 16) & 0xFF;
        int expected = (int) (BetaLightmap.AMBIENT * 255.0F);
        check("pelna ciemnosc ma podloge " + expected + " (jest " + r + ")", r >= expected);
        check("alpha ustawione", (argb >>> 24) == 0xFF);
    }

    /** Mgla liniowa startuje na 25% dystansu. */
    private static void testFogStartsAtQuarter() {
        BetaFog.Shape s = BetaFog.surface(256.0F);
        check("fog start == 64 (jest " + s.start() + ")", Math.abs(s.start() - 64.0F) < 1e-4);
        check("fog end == 256", Math.abs(s.end() - 256.0F) < 1e-4);
        check("mgla powierzchniowa jest liniowa", !s.exponential());
        check("mgla wodna jest wykladnicza", BetaFog.water().exponential());
    }

    /** Void fog skraca widocznosc im nizej, i nic nie zmienia powyzej y=20. */
    private static void testVoidFogDarkens() {
        float atTop = BetaFog.voidFogEnd(256.0F, 20.0D);
        float atBedrock = BetaFog.voidFogEnd(256.0F, 0.0D);
        float midway = BetaFog.voidFogEnd(256.0F, 10.0D);

        check("y=20: bez zmian", Math.abs(atTop - 256.0F) < 1e-4);
        check("y=64: bez zmian", Math.abs(BetaFog.voidFogEnd(256.0F, 64.0D) - 256.0F) < 1e-4);
        check("y=0 mocno skraca (jest " + atBedrock + ")", atBedrock < 50.0F);
        check("monotonicznie rosnie z wysokoscia", atBedrock < midway && midway < atTop);

        check("mnoznik void fog w zakresie 0..1",
                BetaFog.voidFogMultiplier(5.0D, 0.5F) >= 0.0F
                        && BetaFog.voidFogMultiplier(5.0D, 0.5F) <= 1.0F);
        check("powyzej y=20 mnoznik == 1",
                Math.abs(BetaFog.voidFogMultiplier(100.0D, 0.5F) - 1.0F) < 1e-6);
    }

    /** Indeks do grasscolor.png: wilgotnosc mnozona przez temperature. */
    private static void testColorIndex() {
        check("gorac i sucho -> rog (0,255)", BetaColors.colorIndex(1.0, 0.0) == (255 << 8));
        check("zimno i mokro", BetaColors.colorIndex(0.0, 1.0) == ((255 << 8) | 255));
        int mid = BetaColors.colorIndex(0.5, 0.5);
        check("srodek mapy jest w zakresie", mid > 0 && mid < 0xFFFF);
    }

    /** Niebo z bety: blekit z przewaga kanalu niebieskiego, ciemniejace noca. */
    private static void testSkyColor() {
        int day = BetaSky.skyColorByTemperature(0.0F);
        int r = (day >> 16) & 0xFF;
        int g = (day >> 8) & 0xFF;
        int b = day & 0xFF;

        check("niebo jest niebieskie: B > G > R (" + r + "," + g + "," + b + ")",
                b > g && g > r);
        check("niebieski jest pelny (B=" + b + ")", b >= 250);

        // Cieplejszy biom przesuwa odcien ku zieleni -- kanal G rosnie.
        int warm = BetaSky.skyColorByTemperature(2.0F);
        int warmG = (warm >> 8) & 0xFF;
        check("cieplejszy biom ma wiecej zieleni (" + warmG + " > " + g + ")",
                warmG > g);

        // Pora dnia: w poludnie pelny kolor, w nocy czern.
        int noon = BetaSky.skyColor(0.0F, 0.0F);
        int midnight = BetaSky.skyColor(0.0F, 0.5F);
        check("poludnie ma pelny kolor", (noon & 0xFF) >= 250);
        check("polnoc jest czarna (" + midnight + ")", midnight == 0);

        // Zmierzch miedzy nimi -- niebo przyciemnione, ale nie czarne.
        int dusk = BetaSky.skyColor(0.0F, 0.22F);
        int duskB = dusk & 0xFF;
        check("zmierzch jest posredni (B=" + duskB + ")", duskB > 0 && duskB < 250);

        // Konwersja HSV: pelna jasnosc i zero nasycenia to biel.
        check("HSV(dowolny, 0, 1) == biel", BetaSky.hsvToRgb(0.3F, 0.0F, 1.0F) == 0xFFFFFF);
        check("HSV(dowolny, dowolny, 0) == czern", BetaSky.hsvToRgb(0.3F, 1.0F, 0.0F) == 0);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  OK   " : "  BLAD ") + name);
        if (!ok) {
            failures++;
        }
    }
}
