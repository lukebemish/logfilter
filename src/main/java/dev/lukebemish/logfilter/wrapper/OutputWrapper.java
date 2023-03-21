package dev.lukebemish.logfilter.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.gradle.wrapper.GradleWrapperMain;

public class OutputWrapper {
    public static void main(String[] args) throws Exception {
        List<Pattern> remove = new ArrayList<>();
        try (var removeFile = OutputWrapper.class.getClassLoader().getResourceAsStream("META-INF/dev.lukebemish.logfilter/remove")) {
            if (removeFile == null) {
                throw new RuntimeException("No missing regex filtering file");
            }
            try (var reader = new BufferedReader(new InputStreamReader(removeFile))) {
                reader.lines().forEach(line -> {
                    if (!line.isEmpty()) {
                        remove.add(Pattern.compile(line, Pattern.MULTILINE));
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading regex filtering file");
        }

        PrintStream originalOut = System.out;
        var buffer = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                super.flush();
                originalOut.write(process(remove, toString()));
                this.reset();
            }
        };
        var newOut = new PrintStream(buffer, true);

        try (newOut) {
            System.setOut(newOut);
            runMain(args);
        }
    }

    private static void runMain(String[] args) throws Exception {
        GradleWrapperMain.main(args);
    }
    private static byte[] process(List<Pattern> except, String buf) {
        for (var pattern : except) {
            buf = pattern.matcher(buf).replaceAll("");
        }
        return buf.getBytes();
    }
}
