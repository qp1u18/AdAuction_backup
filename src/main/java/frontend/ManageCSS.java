package frontend;

public class ManageCSS {

    static String fontSize = "14";
    static String fontStyle = "Arial";

    public static String getFontSizeCSS() {
        return (HomePageController.class.getResource("/fontSizes/size"+getCurrentFontSize()+"Font.css").toExternalForm());
    }

    public static String getCurrentFontSize() {
        return fontSize;
    }

    public static void setFontSize(String newFontSize) {
        fontSize = newFontSize;
    }

    public static String getFontStyleCSS() {
        return (HomePageController.class.getResource("/fontStyles/"+parseFont(getCurrentFontStyle())+"Font.css").toExternalForm());
    }

    public static String getCurrentFontStyle() {
        return fontStyle;
    }

    public static void setFontStyle(String newStyle) {
        fontStyle = newStyle;
    }

    public static String parseFont(String font) {
        return (font.toLowerCase().replace(" ",""));
    }


}
