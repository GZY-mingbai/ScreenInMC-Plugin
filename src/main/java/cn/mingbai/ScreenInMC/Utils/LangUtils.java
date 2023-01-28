package cn.mingbai.ScreenInMC.Utils;

import cn.mingbai.ScreenInMC.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static cn.mingbai.ScreenInMC.Main.getRandom;

public class LangUtils {
    private static FileConfiguration config = null;

    public static void setLanguage(String language) {
        if(language.equals("qps_ploc")){
            language="en_us";
            useQPSPLOC = true;
        }else{
            useQPSPLOC=false;
        }
        try {
            InputStream path = Main.class.getResourceAsStream("/lang/" + language + ".yml");
            InputStreamReader reader = new InputStreamReader(path, StandardCharsets.UTF_8);
            config = YamlConfiguration.loadConfiguration(reader);
            reader.close();
            path.close();
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
    }

    public static String getText(String path) {
        if (config == null) {
            throw new RuntimeException("Language has not been set yet.");
        }
        if(useQPSPLOC){
                return englishToQPSPLOC(config.getString(path));
        }
        return config.getString(path);
    }
    public static class JsonText{
        public String text;
        public String color;
        public boolean bold;
        public boolean italic;
        public boolean underlined;
        public boolean strikethrough;
        public boolean obfuscated;
        public JsonText extra;
        public String translate;
        public String keybind;
    }
    public static final String EMPTY_JSON_TEXT = "{\"text\":\"\"}";
    //qps_ploc
    private static boolean useQPSPLOC = false;
    public static String englishToQPSPLOC(String eng){
        char[] chars = eng.toCharArray();
        boolean pause = false;
        for(int i=0;i<chars.length;i++){
            if(chars[i]=='%'){
                pause = !pause;
            }
            if(pause){
                continue;
            }
            int index = -1;
            for(int j=0;j<QPS_PLOC_CHARS_.length;j++){
                if(chars[i]==QPS_PLOC_CHARS_[j]){
                    index=j;
                }
            }
            if(index!=-1){
                char[] useChars = QPS_PLOC_CHARS[index];
                int p = useChars.length-1;
                if(p==0) {
                    chars[i] = useChars[0];
                }else{
                    chars[i] = useChars[getRandom().nextInt(0, p)];
                }
            }
        }
        return new String(chars);
    }
    //Chars from https://suntrise.github.io/pseudo/
    public static final char[][] QPS_PLOC_CHARS = {
            {'ä','ā','á','ǎ','à','ă','å','ǻ','ã','ǟ','ǡ','ǻ','ȁ','ȃ','ȧ','ᶏ','ḁ','ẚ','ạ','ả','ấ','ầ','ẩ','ẫ','ậ','ắ','ằ','ẳ','ẵ','ặ','ɑ','α','ά','ὰ','ἀ','ἁ','ἂ','ἃ','ἆ','ἇ','ᾂ','ᾃ','ᾰ','ᾱ','ᾲ','ᾳ','ᾴ','ᾶ','ᾷ','ⱥ'/*,'𐓘','𐓙','𐓚'*/},
            {'Ā','Á','Ǎ','À','Â','Ã','Ä','Å','Ǻ','Ά','Ă','Δ','Λ','Д','Ą'},
            {'b','ь','в','Ъ','Б','б','β','ƀ','ƃ','ɓ','ᵬ','ᶀ','ḃ','ḅ','ḇ','ꞗ'},
            {'ß','฿'},
            {'c','ç','ς','ĉ','č','ċ','ć','ĉ','ċ','ƈ','ȼ','¢','ɕ','ḉ',/*'ꞓ',*/'ꞔ'},
            {'Č','Ç','Ĉ','Ć','€'},
            {'d','ď','đ','₫','ð','δ'},
            {'Ď','Ð'},
            {'e','ē','é','ě','è','ê','ĕ','ė','ë','ę','з','ε','έ','э','℮'},
            {'E','Ē','É','Ě','È','Ĕ','Ё','Σ','Έ','Є','Э','З'},
            {'f','ƒ'},
            {'F','₣'},
            {'ḡ','ģ','ǧ','ĝ','ğ','ġ','ǥ','ǵ','ɠ','ᶃ','ꞡ'},
            {'Ḡ','Ǵ','Ǧ','Ĝ','Ğ','Ģ','Ġ','Ɠ','Ǥ','Ꞡ'},
            {'ĥ','ħ','ђ','н'},
            {'H','Ĥ','Ħ'},
            {'ı','ī','í','ǐ','ì','ĭ','î','ï','ί','į','ΐ','ι'},
            {'Ī','Í','Ǐ','Ì','Î','Ï','Ĭ','Ί'},
            {'j'},
            {'J','Ĵ'},
            {'ƙ','κ'},
            {'К'},
            {'ŀ','ļ','ℓ','ĺ','ļ','ľ','ł'},
            {'Ŀ','£','Ļ','Ł','Ĺ'},
            {'m','₥','м'},
            {'M'},
            {'ń','ň','ŉ','η','ή','и','й','ñ','л','п','π'},
            {'Ń','Ň','И','Й','Π','Л'},
            {'ō','ó','ŏ','ò','ô','õ','ö','ő','σ','ø','ǿ'},
            {'Ō','Ó','Ǒ','Ò','Ô','Õ','Ö','Ό','Θ','Ǿ'},
            {'p','ρ','ƥ','φ'},
            {'P','Þ','₽'},
            {'q','ʠ', 'ɋ'},
            {'Q','Ɋ'},
            {'ř','ŗ','г','ѓ','ґ','я'},
            {'Ř','Я','Г','Ґ'},
            {'ś','š','ŝ','ș','ş','ƨ'},
            {'Š','Ş','Ș','§'},
            {'ț','ţ','ť','ŧ','т','τ'},
            {'Ť','Ţ','Ț','Ŧ'},
            {'ū','ú','ǔ','ù','û','ũ','ů','ų','ü','ǖ','ǘ','ǚ','ǜ','ύ','ϋ','ΰ','µ','ц','џ'},
            {'Ū','Ǔ','Ǖ','Ǘ','Ǚ','Ǜ','Ц'},
            {'ν'},
            {'V','V','Ṽ','Ṿ','Ꝟ'},
            {'ẃ','ẁ','ẅ','ŵ','ш','щ','ω','ώ'},
            {'Ẁ','Ẃ','Ẅ','Ŵ','Ш','Щ'},
            {'x','ж'},
            {'X','Ж'},
            {'y','ỳ','ŷ','ч','γ'},
            {'Ϋ','Ÿ','Ŷ','Ỳ','Ύ','Ψ','￥','У','Ў','Ч'},
            {'z','ź','ż','ž','ƶ','ȥ','ʐ','ᵶ','ᶎ','ẑ','ẓ','ẕ','ⱬ'},
            {'Z','Ź','Ż','Ž','Ƶ','Ȥ','Ẓ','Ẕ','Ẑ','Ⱬ'}
        };
    public static final char[] QPS_PLOC_CHARS_ =
            {'a','A','b','B','c','C','d','D','e','E','f','F','g','G','h','H','i','I','j','J','k','K','l','L','m','M','n','N','o','O','p','P','q','Q','r','R','s','S','t','T','u','U','v','V','w','W','x','X','y','Y','z','Z'};
}
