package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final int PORT = 12345;
    private String username;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public ChatClient(String address, String username) {
        this.username = username;
        try {
            socket = new Socket(address, PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF(username);
            new Thread(new ReadMessages()).start();

            Scanner scanner = new Scanner(System.in);
            String message;
            while (!(message = scanner.nextLine()).equalsIgnoreCase("/sair")) {
                output.writeUTF(message);
            }
            output.writeUTF("/sair");
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            socket.close();
            System.out.println("Conexão encerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadMessages implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = input.readUTF();
                    
                    if (message.startsWith("/receivefile ")) {
                        receiveFile(message);
                    } else {
                        System.out.println(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao ler mensagem do servidor: " + e.getMessage());
            }
        }

        private void receiveFile(String message) {
            try {
                String[] parts = message.split(" info-sending-file ");
                if (parts.length < 4) return;

                String sender = parts[1];
                String fileName = parts[2];
                int fileSize = Integer.parseInt(parts[3]);

                System.out.println("Recebendo arquivo " + fileName + " de " + sender);

                byte[] fileData = new byte[fileSize];
                input.readFully(fileData);

                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(fileData);
                }

                System.out.println("Arquivo " + fileName + " recebido e salvo com sucesso.");
            } catch (IOException e) {
                System.err.println("Erro ao receber arquivo: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o endereço do servidor: ");
        String address = scanner.nextLine();
        System.out.print("Digite seu nome de usuário: ");
        String username = scanner.nextLine();
        new ChatClient(address, username);
    }
}

