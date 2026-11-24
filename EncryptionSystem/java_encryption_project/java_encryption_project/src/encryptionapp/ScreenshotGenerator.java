package encryptionapp;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import javax.imageio.ImageIO;

public class ScreenshotGenerator {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        Path baseDir = Paths.get(args.length > 0 ? args[0] : ".").toAbsolutePath();
        Path screenshotsDir = baseDir.resolve("screenshots");
        Path sampleDir = baseDir.resolve("sample_run");
        Files.createDirectories(screenshotsDir);
        Files.createDirectories(sampleDir);

        Path inputFile = sampleDir.resolve("HelloWorld.java");
        Path encryptedFile = sampleDir.resolve("HelloWorld.java.enc");
        Path decryptedFile = sampleDir.resolve("decrypted_HelloWorld.java");

        String sampleJavaCode = "public class HelloWorld {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello, encrypted world!\");\n"
                + "    }\n"
                + "}\n";
        Files.writeString(inputFile, sampleJavaCode, StandardCharsets.UTF_8);

        String password = "Project@123";
        CryptoUtils.encryptFile(inputFile, encryptedFile, password);
        CryptoUtils.decryptFile(encryptedFile, decryptedFile, password);

        String encryptedText = Files.readString(encryptedFile, StandardCharsets.UTF_8);
        String decryptedText = Files.readString(decryptedFile, StandardCharsets.UTF_8);

        saveWindowShot(
                inputFile.toString(),
                encryptedFile.toString(),
                password,
                true,
                sampleJavaCode,
                "Ready state for encryption.\n\nSelect a text or Java file, choose the output path, and press Start Process.",
                screenshotsDir.resolve("screen_1_main_window.png")
        );

        saveWindowShot(
                inputFile.toString(),
                encryptedFile.toString(),
                password,
                true,
                encryptedText,
                "Encryption Completed Successfully.\n\nInput File: " + inputFile + "\n"
                        + "Output File: " + encryptedFile + "\n"
                        + "Password Status: Password applied successfully.\n"
                        + "Transfer Note: The encrypted file can be sent over a public network.",
                screenshotsDir.resolve("screen_2_encryption_output.png")
        );

        saveWindowShot(
                encryptedFile.toString(),
                decryptedFile.toString(),
                password,
                false,
                decryptedText,
                "Decryption Completed Successfully.\n\nInput File: " + encryptedFile + "\n"
                        + "Output File: " + decryptedFile + "\n"
                        + "Password Status: Correct password used.\n"
                        + "Original readable content restored.",
                screenshotsDir.resolve("screen_3_decryption_output.png")
        );
    }

    private static void saveWindowShot(String inputPath,
                                       String outputPath,
                                       String password,
                                       boolean encryptMode,
                                       String previewText,
                                       String statusText,
                                       Path outputImage) throws IOException, GeneralSecurityException {
        FileEncryptionApp app = new FileEncryptionApp();
        app.setDemoState(inputPath, outputPath, password, encryptMode, previewText, statusText);
        app.setSize(980, 680);
        app.doLayout();
        app.validate();

        Container root = app.getRootUi();
        root.setSize(980, 680);
        root.doLayout();
        layoutTree(root);

        BufferedImage image = new BufferedImage(980, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        root.printAll(g2);
        g2.dispose();

        ImageIO.write(image, "png", outputImage.toFile());
    }

    private static void layoutTree(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                child.doLayout();
                layoutTree(child);
            }
        }
    }
}