package encryptionapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class FileEncryptionApp extends JFrame {

    private final JTextField inputFileField = new JTextField();
    private final JTextField outputFileField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextArea previewArea = new JTextArea();
    private final JTextArea statusArea = new JTextArea();
    private final JRadioButton encryptOption = new JRadioButton("Encrypt File", true);
    private final JRadioButton decryptOption = new JRadioButton("Decrypt File");
    private final JButton processButton = new JButton("Start Process");

    public FileEncryptionApp() {
        super("Text File Encryption Decryption Project");
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(contentPanel);

        JLabel titleLabel = new JLabel("Password Based File Encryption / Decryption System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Project Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(encryptOption);
        buttonGroup.add(decryptOption);

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Mode:"), gbc);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modePanel.add(encryptOption);
        modePanel.add(decryptOption);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        formPanel.add(modePanel, gbc);

        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Input File:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputFileField.setEditable(false);
        formPanel.add(inputFileField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseInputButton = new JButton("Browse...");
        browseInputButton.addActionListener(this::browseInputFile);
        formPanel.add(browseInputButton, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Output File:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        outputFileField.setEditable(false);
        formPanel.add(outputFileField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseOutputButton = new JButton("Save As...");
        browseOutputButton.addActionListener(this::browseOutputFile);
        formPanel.add(browseOutputButton, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        formPanel.add(passwordField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        processButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        processButton.addActionListener(this::processFile);
        formPanel.add(processButton, gbc);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("SansSerif", Font.PLAIN, 13));

        centerPanel.add(wrapInScrollPane(previewArea, "File Preview / Cipher Text Preview"));
        centerPanel.add(wrapInScrollPane(statusArea, "Execution Status"));

        contentPanel.add(formPanel, BorderLayout.WEST);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        statusArea.setText("1. Choose Encrypt or Decrypt mode.\n"
                + "2. Select the input file.\n"
                + "3. Choose where to save the output file.\n"
                + "4. Enter the password.\n"
                + "5. Click Start Process.");
    }

    private JScrollPane wrapInScrollPane(JTextArea textArea, String title) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    private void browseInputFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Input File");
        chooser.setFileFilter(new FileNameExtensionFilter("Text and Java Files", "txt", "java", "enc"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath();
            inputFileField.setText(selected.toString());
            loadPreview(selected);
            suggestOutputFile(selected);
        }
    }

    private void browseOutputFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Output File");
        if (!outputFileField.getText().isBlank()) {
            chooser.setSelectedFile(Paths.get(outputFileField.getText()).toFile());
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void suggestOutputFile(Path inputPath) {
        String name = inputPath.getFileName().toString();
        Path parent = inputPath.toAbsolutePath().getParent();
        if (encryptOption.isSelected()) {
            outputFileField.setText(parent.resolve(name + ".enc").toString());
        } else {
            String cleaned = name.endsWith(".enc") ? name.substring(0, name.length() - 4) : name + ".decrypted.txt";
            outputFileField.setText(parent.resolve("decrypted_" + cleaned).toString());
        }
    }

    private void loadPreview(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            previewArea.setText(limitPreview(content));
        } catch (IOException ex) {
            previewArea.setText("Unable to read preview: " + ex.getMessage());
        }
    }

    private String limitPreview(String content) {
        int maxLength = 1400;
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "\n\n... preview truncated ...";
    }

    private void processFile(ActionEvent event) {
        String input = inputFileField.getText().trim();
        String output = outputFileField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (input.isEmpty() || output.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select input file, output file, and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path inputFile = Paths.get(input);
            Path outputFile = Paths.get(output);

            if (encryptOption.isSelected()) {
                CryptoUtils.encryptFile(inputFile, outputFile, password);
                String encryptedPreview = Files.readString(outputFile, StandardCharsets.UTF_8);
                previewArea.setText(limitPreview(encryptedPreview));
                statusArea.setText("Encryption Completed Successfully.\n\n"
                        + "Input File: " + inputFile + "\n"
                        + "Output File: " + outputFile + "\n"
                        + "Password Status: Password applied successfully.\n"
                        + "Transfer Note: You can now send the encrypted file over a public network.");
                JOptionPane.showMessageDialog(this,
                        "File encrypted successfully and saved to disk.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                CryptoUtils.decryptFile(inputFile, outputFile, password);
                String decryptedPreview = Files.readString(outputFile, StandardCharsets.UTF_8);
                previewArea.setText(limitPreview(decryptedPreview));
                statusArea.setText("Decryption Completed Successfully.\n\n"
                        + "Input File: " + inputFile + "\n"
                        + "Output File: " + outputFile + "\n"
                        + "Password Status: Correct password used.\n"
                        + "Original readable content restored.");
                JOptionPane.showMessageDialog(this,
                        "File decrypted successfully and restored.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (GeneralSecurityException ex) {
            statusArea.setText("Security Error:\n" + ex.getMessage()
                    + "\n\nPossible reason: wrong password or damaged encrypted file.");
            JOptionPane.showMessageDialog(this,
                    "Decryption/Encryption failed: " + ex.getMessage(),
                    "Security Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            statusArea.setText("File Error:\n" + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "File operation failed: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setDemoState(String inputPath, String outputPath, String password, boolean encryptMode,
                             String previewText, String statusText) {
        encryptOption.setSelected(encryptMode);
        decryptOption.setSelected(!encryptMode);
        inputFileField.setText(inputPath);
        outputFileField.setText(outputPath);
        passwordField.setText(password);
        previewArea.setText(previewText);
        statusArea.setText(statusText);
    }

    public Container getRootUi() {
        return getContentPane();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileEncryptionApp().setVisible(true));
    }
}