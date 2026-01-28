package com.moneybuddy.moneylog.mobti.util;

import java.util.HashMap;
import java.util.Map;

public final class MobtiMascot {
    public static final class Info {
        public final String emoji;
        public final String animalKo;
        Info(String emoji, String animalKo) {
            this.emoji = emoji;
            this.animalKo = animalKo;
        }
    }

    private static final Map<String, Info> MAP = new HashMap<>();
    static {
        MAP.put("IMTP", new Info("🐿️", "다람쥐"));
        MAP.put("IMTR", new Info("🐢", "거북이"));
        MAP.put("IMCP", new Info("🦉", "올빼미"));
        MAP.put("IMCR", new Info("🐂", "황소"));
        MAP.put("ISTP", new Info("🐱", "고양이"));
        MAP.put("ISTR", new Info("🦩", "홍학"));
        MAP.put("ISCP", new Info("🐕", "개"));
        MAP.put("ISCR", new Info("🐒", "원숭이"));
        MAP.put("EMTP", new Info("🦫", "비버"));
        MAP.put("EMTR", new Info("🦔", "고슴도치"));
        MAP.put("EMCP", new Info("🐧", "펭귄"));
        MAP.put("EMCR", new Info("🐼", "판다"));
        MAP.put("ESTP", new Info("🦊", "여우"));
        MAP.put("ESTR", new Info("🦅", "독수리"));
        MAP.put("ESCP", new Info("🐘", "코끼리"));
        MAP.put("ESCR", new Info("🐴", "말"));
    }

    public static String emoji(String code) {
        if (code == null) return "❓";
        Info i = MAP.get(code.toUpperCase());
        return i != null ? i.emoji : "❓";
    }

    public static String animal(String code) {
        if (code == null) return "?";
        Info i = MAP.get(code.toUpperCase());
        return i != null ? i.animalKo : "?";
    }

    // "실속꾼 펭귄" 같은 조합 반환
    public static String nicknameWithAnimal(String nickname, String code) {
        String ani = animal(code);
        if (nickname == null || nickname.isBlank()) return ani.equals("?") ? "" : ani;
        return ani.equals("?") ? nickname : (nickname + " " + ani);
    }

    private MobtiMascot() {}
}
