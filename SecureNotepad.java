import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecureNotepad extends JFrame {

    private final JTextArea textArea;
    private final JComboBox<String> fontComboBox;
    private final JComboBox<Integer> fontSizeComboBox;
    private final JComboBox<String> styleComboBox;
    private static final String SECRET_KEY = "MySecretKey12345";

    public SecureNotepad() {
        setTitle("защищенный блокнот");
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        JButton saveButton = new JButton("сохранить");
        JButton decryptButton = new JButton("расшифровать");

        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fontNames);

        Integer[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
        fontSizeComboBox = new JComboBox<>(fontSizes);

        String[] styles = {"обычный", "полужирный", "курсив", "полужирный курсив"};
        styleComboBox = new JComboBox<>(styles);

        fontComboBox.addActionListener(e -> {
            String fontName = (String) fontComboBox.getSelectedItem();
            textArea.setFont(new Font(fontName, Font.PLAIN, (int) fontSizeComboBox.getSelectedItem()));
        });

        fontSizeComboBox.addActionListener(e -> {
            String fontName = (String) fontComboBox.getSelectedItem();
            textArea.setFont(new Font(fontName, Font.PLAIN, (int) fontSizeComboBox.getSelectedItem()));
        });

        styleComboBox.addActionListener(e -> updateFont());

        JPanel fontPanel = new JPanel();
        fontPanel.add(fontComboBox);
        fontPanel.add(fontSizeComboBox);
        fontPanel.add(styleComboBox);
        add(fontPanel, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(decryptButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveToFile());

        decryptButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("выберите файл для расшифровки");
            int userSelection = fileChooser.showOpenDialog(SecureNotepad.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToDecrypt = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(fileToDecrypt))) {
                    String encryptedText = reader.readLine();
                    String decryptedText = decrypt(encryptedText);
                    textArea.setText(decryptedText);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SecureNotepad.this, "ошибка чтения файла", "ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        textArea.setFont(new Font("Arial", Font.PLAIN, 20));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateFont() {
        String fontName = (String) fontComboBox.getSelectedItem();
        int fontStyle = Font.PLAIN; // стиль по умолчанию
        switch ((String) styleComboBox.getSelectedItem()) {
            case "полужирный" -> fontStyle = Font.BOLD;
            case "курсив" -> fontStyle = Font.ITALIC;
            case "полужирный курсив" -> fontStyle = Font.BOLD + Font.ITALIC;
        }
        textArea.setFont(new Font(fontName, fontStyle, (int) fontSizeComboBox.getSelectedItem()));
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("выберите файл для сохранения");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave.getAbsolutePath() + ".txt"))) {
                String plainText = textArea.getText();
                String encryptedText = encrypt(plainText);
                writer.write(encryptedText);
                JOptionPane.showMessageDialog(this, "файл успешно сохранен", "сохранение", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "ошибка сохранения файла", "ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SecureNotepad::new);
    }
}