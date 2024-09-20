package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utilities {
    private static final String log_File = "logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logClientConnection(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "Cliente conectado: " + clientAddress + "às " + timestamp;

        writeLog(logEntry);
    }

    private static void writeLog(String logEntry) {
        try (FileWriter fileWriter = new FileWriter(log_File, true);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(logEntry);
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    // Impressão dos logs no console, se necessário
    // public static void readLogs() {
    // try (BufferedReader reader = new BufferedReader(new FileReader(log_File))) {
    // String line;
    // while ((line = reader.readLine())!= null) {
    // System.out.println(line);
    // }
    // } catch (IOException e) {
    // System.err.println("Erro ao ler log: " + e.getMessage());
    // }
    // }

    public static byte[] fileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        }
        return fileBytes;
    }

    public static void bytesToFile(byte[] fileBytes, String fileName) throws IOException {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String outputFilePath = currentDir + File.separator + fileName;
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(fileBytes);
            System.out.println("Arquivo salvo com sucesso: " + outputFilePath);
        }
    }
}
