package cn.mingbai.ScreenInMC.Utils;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.JsonTextToNMSComponent;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils.JSONArray;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils.JSONObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.mingbai.ScreenInMC.Main.getRandom;

public class LangUtils {
    //Chars from https://suntrise.github.io/pseudo/
    public static final char[][] QPS_PLOC_CHARS = {
            {'ä', 'ā', 'á', 'ǎ', 'à', 'ă', 'å', 'ǻ', 'ã', 'ǟ', 'ǡ', 'ǻ', 'ȁ', 'ȃ', 'ȧ', 'ᶏ', 'ḁ', 'ẚ', 'ạ', 'ả', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ', 'ɑ', 'α', 'ά', 'ὰ', 'ἀ', 'ἁ', 'ἂ', 'ἃ', 'ἆ', 'ἇ', 'ᾂ', 'ᾃ', 'ᾰ', 'ᾱ', 'ᾲ', 'ᾳ', 'ᾴ', 'ᾶ', 'ᾷ', 'ⱥ'/*,'𐓘','𐓙','𐓚'*/},
            {'Ā', 'Á', 'Ǎ', 'À', 'Â', 'Ã', 'Ä', 'Å', 'Ǻ', 'Ά', 'Ă', 'Δ', 'Λ', 'Д', 'Ą'},
            {'b', 'ь', 'в', 'Ъ', 'Б', 'б', 'β', 'ƀ', 'ƃ', 'ɓ', 'ᵬ', 'ᶀ', 'ḃ', 'ḅ', 'ḇ', 'ꞗ'},
            {'ß', '฿'},
            {'c', 'ç', 'ς', 'ĉ', 'č', 'ċ', 'ć', 'ĉ', 'ċ', 'ƈ', 'ȼ', '¢', 'ɕ', 'ḉ',/*'ꞓ',*/'ꞔ'},
            {'Č', 'Ç', 'Ĉ', 'Ć', '€'},
            {'d', 'ď', 'đ', '₫', 'ð', 'δ'},
            {'Ď', 'Ð'},
            {'e', 'ē', 'é', 'ě', 'è', 'ê', 'ĕ', 'ė', 'ë', 'ę', 'з', 'ε', 'έ', 'э', '℮'},
            {'E', 'Ē', 'É', 'Ě', 'È', 'Ĕ', 'Ё', 'Σ', 'Έ', 'Є', 'Э', 'З'},
            {'f', 'ƒ'},
            {'F', '₣'},
            {'ḡ', 'ģ', 'ǧ', 'ĝ', 'ğ', 'ġ', 'ǥ', 'ǵ', 'ɠ', 'ᶃ', 'ꞡ'},
            {'Ḡ', 'Ǵ', 'Ǧ', 'Ĝ', 'Ğ', 'Ģ', 'Ġ', 'Ɠ', 'Ǥ', 'Ꞡ'},
            {'ĥ', 'ħ', 'ђ', 'н'},
            {'H', 'Ĥ', 'Ħ'},
            {'ı', 'ī', 'í', 'ǐ', 'ì', 'ĭ', 'î', 'ï', 'ί', 'į', 'ΐ', 'ι'},
            {'Ī', 'Í', 'Ǐ', 'Ì', 'Î', 'Ï', 'Ĭ', 'Ί'},
            {'j'},
            {'J', 'Ĵ'},
            {'ƙ', 'κ'},
            {'К'},
            {'ŀ', 'ļ', 'ℓ', 'ĺ', 'ļ', 'ľ', 'ł'},
            {'Ŀ', '£', 'Ļ', 'Ł', 'Ĺ'},
            {'m', '₥', 'м'},
            {'M'},
            {'ń', 'ň', 'ŉ', 'η', 'ή', 'и', 'й', 'ñ', 'л', 'п', 'π'},
            {'Ń', 'Ň', 'И', 'Й', 'Π', 'Л'},
            {'ō', 'ó', 'ŏ', 'ò', 'ô', 'õ', 'ö', 'ő', 'σ', 'ø', 'ǿ'},
            {'Ō', 'Ó', 'Ǒ', 'Ò', 'Ô', 'Õ', 'Ö', 'Ό', 'Θ', 'Ǿ'},
            {'p', 'ρ', 'ƥ', 'φ'},
            {'P', 'Þ', '₽'},
            {'q', 'ʠ', 'ɋ'},
            {'Q', 'Ɋ'},
            {'ř', 'ŗ', 'г', 'ѓ', 'ґ', 'я'},
            {'Ř', 'Я', 'Г', 'Ґ'},
            {'ś', 'š', 'ŝ', 'ș', 'ş', 'ƨ'},
            {'Š', 'Ş', 'Ș', '§'},
            {'ț', 'ţ', 'ť', 'ŧ', 'т', 'τ'},
            {'Ť', 'Ţ', 'Ț', 'Ŧ'},
            {'ū', 'ú', 'ǔ', 'ù', 'û', 'ũ', 'ů', 'ų', 'ü', 'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ύ', 'ϋ', 'ΰ', 'µ', 'ц', 'џ'},
            {'Ū', 'Ǔ', 'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ц'},
            {'ν'},
            {'V', 'V', 'Ṽ', 'Ṿ', 'Ꝟ'},
            {'ẃ', 'ẁ', 'ẅ', 'ŵ', 'ш', 'щ', 'ω', 'ώ'},
            {'Ẁ', 'Ẃ', 'Ẅ', 'Ŵ', 'Ш', 'Щ'},
            {'x', 'ж'},
            {'X', 'Ж'},
            {'y', 'ỳ', 'ŷ', 'ч', 'γ'},
            {'Ϋ', 'Ÿ', 'Ŷ', 'Ỳ', 'Ύ', 'Ψ', '￥', 'У', 'Ў', 'Ч'},
            {'z', 'ź', 'ż', 'ž', 'ƶ', 'ȥ', 'ʐ', 'ᵶ', 'ᶎ', 'ẑ', 'ẓ', 'ẕ', 'ⱬ'},
            {'Z', 'Ź', 'Ż', 'Ž', 'Ƶ', 'Ȥ', 'Ẓ', 'Ẕ', 'Ẑ', 'Ⱬ'}
    };
    public static final char[] QPS_PLOC_CHARS_ =
            {'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F', 'g', 'G', 'h', 'H', 'i', 'I', 'j', 'J', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'p', 'P', 'q', 'Q', 'r', 'R', 's', 'S', 't', 'T', 'u', 'U', 'v', 'V', 'w', 'W', 'x', 'X', 'y', 'Y', 'z', 'Z'};
    private static FileConfiguration config = null;
    //qps_ploc
    private static boolean useQPSPLOC = false;

    public static void setLanguage(String language) {
        if (language.equals("qps_ploc")) {
            language = "en_us";
            useQPSPLOC = true;
        } else {
            useQPSPLOC = false;
        }
        try {
            InputStream path = Main.class.getResourceAsStream("/lang/" + language + ".yml");
            InputStreamReader reader = new InputStreamReader(path, StandardCharsets.UTF_8);
            config = YamlConfiguration.loadConfiguration(reader);
            reader.close();
            path.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getText(String path) {
        if (config == null) {
            throw new RuntimeException("Language has not been set yet.");
        }
        if (useQPSPLOC) {
            return englishToQPSPLOC(config.getString(path));
        }
        return config.getString(path);
    }

    public static String englishToQPSPLOC(String eng) {
        char[] chars = eng.toCharArray();
        boolean pause = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                pause = !pause;
            }
            if (pause) {
                continue;
            }
            int index = -1;
            for (int j = 0; j < QPS_PLOC_CHARS_.length; j++) {
                if (chars[i] == QPS_PLOC_CHARS_[j]) {
                    index = j;
                }
            }
            if (index != -1) {
                char[] useChars = QPS_PLOC_CHARS[index];
                int p = useChars.length - 1;
                if (p == 0) {
                    chars[i] = useChars[0];
                } else {
                    chars[i] = useChars[getRandom().nextInt(p+1)];
                }
            }
        }
        return new String(chars);
    }

    public static class JsonText {
        public static class ClickEvent{
            public String action;
            public String value;
            public ClickEvent(String action,String value){
                this.action=action;
                this.value=value;
            }
        }
        public static class HoverEvent{
            public String action;
            public Map contents;
        }

        public String text=null;
        public String color=null;
        public Boolean bold=null;
        public Boolean italic=null;
        public Boolean underlined=null;
        public Boolean strikethrough=null;
        public Boolean obfuscated=null;
        public JsonText extra=null;
        public String translate=null;
        public String keybind=null;
        public ClickEvent clickEvent=null;
//        public HoverEvent hoverEvent = null;
//        public ResourceLocation font = null;

        public JsonText setTranslate(String translate) {
            this.translate = translate;
            return this;
        }

        public JsonText setStrikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public JsonText setObfuscated(boolean obfuscated) {
            this.obfuscated = obfuscated;
            return this;
        }

        public JsonText setUnderlined(boolean underlined) {
            this.underlined = underlined;
            return this;
        }

        public JsonText setText(String text) {
            this.text = text;
            return this;
        }

        public JsonText setKeybind(String keybind) {
            this.keybind = keybind;
            return this;
        }

        public JsonText setItalic(boolean italic) {
            this.italic = italic;
            return this;
        }

        public JsonText setExtra(JsonText extra) {
            this.extra = extra;
            return this;
        }

        public JsonText setColor(String color) {
            this.color = color;
            return this;
        }

        public JsonText setBold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public JsonText setClickEvent(ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
            return this;
        }


//        public JsonText setHoverEvent(HoverEvent hoverEvent) {
//            this.hoverEvent = hoverEvent;
//            return this;
//        }

        public JsonText(){}
        public JsonText(String text){
            this.text = text;
        }
        public JsonText disableRichStringInOldVersion(){
            this.disableRichStringInOldVersion = true;
            return this;
        }
        private boolean disableRichStringInOldVersion = false;
        private JSONObject getJSONObject(){
            JSONObject jsonText = new JSONObject();
            if (text != null) {
                jsonText.setValue("text",text);
            }
            if (color != null) {
                jsonText.setValue("color",color);
            }
            if (bold != null) {
                jsonText.setValue("bold",bold);
            }
            if (italic != null) {
                jsonText.setValue("italic",italic);
            }
            if (underlined != null) {
                jsonText.setValue("underlined",underlined);
            }
            if (strikethrough != null) {
                jsonText.setValue("strikethrough",strikethrough);
            }
            if (obfuscated != null) {
                jsonText.setValue("obfuscated",obfuscated);
            }
            if (extra != null) {
                jsonText.setValue("extra",extra.getJSONObject());
            }
            if (translate != null) {
                jsonText.setValue("translate",translate);
            }
            if (keybind != null) {
                jsonText.setValue("keybind",keybind);
            }
            if(clickEvent!=null){
                JSONObject object = new JSONObject();
                object.setValue("action",clickEvent.action);
                object.setValue("value",clickEvent.value);
                jsonText.setValue("clickEvent",object);
            }
            return jsonText;
        }
        public String toJSON(){
            return Main.getJSONUtils().toJson(getJSONObject());
        }
        public JsonText copy(){
            JsonText jsonText = new JsonText();
            jsonText.text=text;
            jsonText.color=color;
            jsonText.bold=bold;
            jsonText.italic=italic;
            jsonText.underlined=underlined;
            jsonText.strikethrough=strikethrough;
            jsonText.obfuscated=obfuscated;
            jsonText.extra=extra;
            jsonText.translate=translate;
            jsonText.keybind=keybind;
            jsonText.clickEvent=clickEvent;
//            jsonText.hoverEvent = hoverEvent;
            return jsonText;
        }
        public List<JsonText> toListWithoutExtra(){
            List<JsonText> list = new ArrayList<>();
            JsonText newJsonText = this.copy();
            newJsonText.extra=null;
            list.add(newJsonText);
            if(extra!=null){
                list.addAll(extra.toListWithoutExtra());
            }
            return list;
        }
        public String toJSONWithoutExtra(){
            return toJSON(toListWithoutExtra().toArray(new JsonText[0]));
        }
        public static String toJSON(JsonText[] jsonTexts){
            JSONArray array = new JSONArray();
            for(int i=0;i<jsonTexts.length;i++){
                array.add(jsonTexts[i].getJSONObject());
            }
            return Main.getJSONUtils().toJson(array);
        }
        public static String toJSON(JsonText jsonText){
            return jsonText.toJSON();
        }
        public Object toComponent(){
            return JsonTextToNMSComponent.jsonTextToComponent(this);
        }
        public JsonText addExtra(JsonText extra){
            if(this.extra!=null){
                this.extra.addExtra(extra);
            }else{
                this.extra = extra;
            }
            return this;
        }
        public String toRichString(){
            String str = "";
            if(!disableRichStringInOldVersion) {
                str += "§r";
                if (color != null) {
                    switch (color) {
                        case "black":
                            str += "§0";
                            break;
                        case "dark_blue":
                            str += "§1";
                            break;
                        case "dark_green":
                            str += "§2";
                            break;
                        case "dark_aqua":
                            str += "§3";
                            break;
                        case "dark_red":
                            str += "§4";
                            break;
                        case "dark_purple":
                            str += "§5";
                            break;
                        case "gold":
                            str += "§6";
                            break;
                        case "gray":
                            str += "§7";
                            break;
                        case "dark_gray":
                            str += "§8";
                            break;
                        case "blue":
                            str += "§9";
                            break;
                        case "green":
                            str += "§a";
                            break;
                        case "aqua":
                            str += "§b";
                            break;
                        case "red":
                            str += "§c";
                            break;
                        case "light_purple":
                            str += "§d";
                            break;
                        case "yellow":
                            str += "§e";
                            break;
                        case "white":
                            str += "§f";
                            break;
                    }
                }
                if (bold != null && bold == true) {
                    str += "§l";
                }
                if (italic != null && italic == true) {
                    str += "§o";
                }
                if (underlined != null && underlined == true) {
                    str += "§n";
                }
                if (strikethrough != null && strikethrough == true) {
                    str += "§m";
                }
                if (obfuscated != null && obfuscated == true) {
                    str += "§k";
                }
            }
            if(text!=null){
                str+=text;
            }else
            if(keybind!=null){
                str+=JsonTextToNMSComponent.getKeybind(keybind);
            }else
            if(translate!=null) {
                str+=translate;
            }
            if(extra!=null){
                str+=extra.toRichString();
            }
            return str;

        }
    }
}
