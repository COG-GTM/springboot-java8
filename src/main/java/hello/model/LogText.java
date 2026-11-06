package hello.model;

final class LogText {

    private LogText() {
    }

    static String clean(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean unsafe = Character.isISOControl(c) || c == '\u2028' || c == '\u2029';
            out.append(unsafe ? '_' : c);
        }
        return out.toString();
    }
}
