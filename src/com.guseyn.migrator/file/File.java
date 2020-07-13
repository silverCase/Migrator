package file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class File {
    public static String content(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        String currentLine;
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            while ((currentLine = bufferedReader.readLine()) != null) {
                content.append(currentLine);
            }
        }
        return content.toString();
    }
}
