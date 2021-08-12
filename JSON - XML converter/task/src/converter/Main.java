package converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    final static String ID = "\\w[\\w\\d]*";
    final static String ATTR_KEY = "\\\"@" + ID + "\\\"";
    final static String CONTENT_KEY = "\\\"#" + ID + "\\\"";
    final static String ANYTHING = "\"[^\"]*\"";
    final static String VALUE = "(" + ANYTHING + "|null|\\d+)";

    public static String removeSpaces(String input) {
        String text = Pattern.compile("\\s*:\\s*").matcher(input).replaceAll(":");
        text = text.replaceAll("\\{\\s*", "{");
        text = text.replaceAll("\\s*\\}", "}");
        text = text.replaceAll("\\s*\"", "\"");
        return text;
    }

    public static String findMathesElement(String pattern, String where) {
        Matcher matcher = Pattern.compile(pattern).matcher(where);
        return matcher.find() ? matcher.group() : null;
    }

    public static void addAllAttributes(StringBuilder sb, String attributeStr) {

        Matcher matcher = Pattern.compile(ATTR_KEY + ":" + VALUE).matcher(attributeStr);

        while (matcher.find()) {
            String atr = matcher.group();

            atr = atr.replaceAll("\\\"@", "");
            atr = atr.replaceAll("\\\":", "=");

            String[] words = atr.split("=");
            if (words[1].charAt(0) != '\"') {
                sb.append(" " + words[0] + "=" + "\"" + words[1] + "\"");
            } else {
                sb.append(" " + atr);
            }

        }
    }

    public static void addContentToXML(String content, String tag, StringBuilder sb) {
        if (content.equals("null")) {
            sb.append("/>");
        } else {
            sb.append(">" + content + "</" + tag + ">");
        }
    }

    public static String convertJSONToXML(String input) {
        StringBuilder sb = new StringBuilder();

        String text = removeSpaces(input);

        Pattern keyValuePairPattern = Pattern.compile("\"[@#]?" + ID + "\":" + VALUE);
        Pattern objectPattern = Pattern.compile("\"" + ID + "\":\\{"
                + "(" + keyValuePairPattern + ",?" + ")+" + "\\}");
        Matcher objectMatcher = objectPattern.matcher(text);

        if (objectMatcher.find()) {

            do {
                String object = objectMatcher.group();

                String tag = findMathesElement(ID, object);
                sb.append("<" + tag);

                addAllAttributes(sb, object);

                String content =findMathesElement(CONTENT_KEY + ":" + VALUE, object);
                content = content.replaceAll(CONTENT_KEY + ":|\"", "");

                addContentToXML(content, tag, sb);

            } while (objectMatcher.find());

        } else {

            objectMatcher = Pattern.compile("\"" + ID + "\":" + VALUE).matcher(text);

            while (objectMatcher.find()) {

                String keyValuePair = objectMatcher.group();

                String key = findMathesElement(ID, keyValuePair);
                sb.append("<" + key);

                String content = findMathesElement(":" + VALUE, keyValuePair).substring(1).replaceAll("\"", "");

                addContentToXML(content, key, sb);

            }

        }



        return sb.toString();
    }

    public static void addContentToJSON(String keyValuePair, StringBuilder sb, boolean hasAttr, String key) {

        Matcher valueMathcer = Pattern.compile(">[^<>]+<").matcher(keyValuePair);

        if (hasAttr) {
            sb.append("\t\t\"#" + key + "\" : ");
        }
        if (valueMathcer.find())  {
            String value = valueMathcer.group();
            sb.append("\"" + value.substring(1,value.length()-1) + "\"\n");
        } else {
            sb.append("null\n");
        }

    }

    public static String convertXMLToJSON(String text) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        Pattern attributePattern = Pattern.compile("\\s+" + ID + "\\s*=\\s*\"([^<>\"])+\"");
        Pattern tagPattern = Pattern.compile("<" + ID
                + "(" + attributePattern + ")*" +
                "(\\s*>([^<>])+</" + ID + ">|\\s*/>)");
        Matcher matcher = tagPattern.matcher(text);


        while (matcher.find()) {

            String keyValuePair = matcher.group();

            String key = findMathesElement(ID, keyValuePair);
            sb.append("\t\"" + key + "\" : ");

            Matcher attributesIterator = attributePattern.matcher(keyValuePair);

            if (attributesIterator.find()) {

                sb.append("{\n");

                do {
                    String atrStr = attributesIterator.group();
                    String attrName = findMathesElement(ID, atrStr);
                    String attrValue = findMathesElement(ANYTHING ,atrStr);

                    sb.append("\t\t\"@" + attrName + "\" : " + attrValue + ",\n");

                } while (attributesIterator.find());

                addContentToJSON(keyValuePair, sb,true, key);

                sb.append("\t}\n");

            } else {
                addContentToJSON(keyValuePair, sb, false, null);
            }


        }

        sb.append("\n}");

        return sb.toString();
    }

    public static String input() throws FileNotFoundException {
        File file = new File("test.txt");
        Scanner sc = new Scanner(file);
        String str = sc.useDelimiter("\\Z").next();
        sc.close();
        return str;
    }

    public static void main(String[] args) throws FileNotFoundException {

        String inputStr = input();

        Matcher matcherJSON = Pattern.compile("\\s*\\{").matcher(inputStr);
        Matcher matcherXML = Pattern.compile("\\s*<").matcher(inputStr);

        if (matcherJSON.find()) {
            System.out.println(convertJSONToXML(inputStr));
        } else if (matcherXML.find()) {
            System.out.println(convertXMLToJSON(inputStr));
        } else {
            System.out.println("Not supported format");
        }

    }
}
