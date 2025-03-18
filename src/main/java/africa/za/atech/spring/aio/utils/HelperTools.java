package africa.za.atech.spring.aio.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
public class HelperTools {

    public static String wrapVar(String text) {
        return "'" + text + "'";
    }

    @SneakyThrows
    public static String getString(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        return IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
    }

    public static JsonArray getJsonArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    public static JsonArray getJsonArray(String json, String node) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        if (jsonObject.has(node)) {
            try {
                return jsonObject.getAsJsonArray(node);
            } catch (ClassCastException e) {
                throw new ClassCastException("JsonObject found at node: '" + node + "'");
            }
        } else {
            throw new RuntimeException("Node '" + node + "' does not exist at root");
        }
    }

    @SneakyThrows
    public static List<String[]> readCsv(File csvFile) {
        return new CSVReader(new FileReader(csvFile)).readAll();
    }

    @SneakyThrows
    public static List<String[]> readCsv(MultipartFile csvFile, String tempPath) {
        return readCsv(toFile(csvFile, tempPath));
    }

    @SuppressWarnings("all")
    public static String toHtml(String text) {
        text = "\n\n" + text;
        Document doc = Jsoup.parse("<div></div>");
        Element div = doc.select("div").first();
        String[] paragraphs = text.split("\n\n");
        for (String paragraph : paragraphs) {
            if (paragraph.startsWith("\n- ")) {
                String[] listItems = paragraph.split("\n- ");
                div.append("<ul>");
                for (String item : listItems) {
                    if (!item.isEmpty()) {
                        div.append("<li>" + formatText(item) + "</li>");
                    }
                }
                div.append("</ul>");
            } else {
                div.append("<p>" + formatText(paragraph) + "</p>");
            }
        }
        return doc.toString();
    }


    private static String formatText(String text) {
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        return text;
    }

    public static File toFile(MultipartFile multipartFile, String location) {
        File destFile = new File(location + "/" + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(destFile);
        } catch (IOException e) {
            log.warn("Unable to transfer multipart FILE to: {}", location);
        }
        return destFile;
    }

    public static String generatePassword(int length, int numberOfDigits, int numberOfSpecialCharacters) {
        if (length < 3 || numberOfDigits + numberOfSpecialCharacters > length - 2) {
            throw new IllegalArgumentException("Invalid parameters. Length must be at least 3, and total number of digits and special characters must not exceed length - 2.");
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = upperCase.toLowerCase();
        String digits = "0123456789";
        String specialCharacters = "!@#%&*_?|()[]{}";
        StringBuilder allCharacters = new StringBuilder(upperCase + lowerCase);

        for (int i = 0; i < numberOfDigits; i++) {
            allCharacters.append(digits);
        }

        for (int i = 0; i < numberOfSpecialCharacters; i++) {
            allCharacters.append(specialCharacters);
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        for (int i = 0; i < length - 3; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        char[] passwordChars = password.toString().toCharArray();
        for (int i = passwordChars.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordChars[index];
            passwordChars[index] = passwordChars[i];
            passwordChars[i] = temp;
        }

        return new String(passwordChars);
    }

    public static String getLoggedInUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}