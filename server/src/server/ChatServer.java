package server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    private static final String LOG_FILE = "server_log.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de chat iniciado na porta " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private String clientName;
        private DataInputStream input;
        private DataOutputStream output;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                clientName = input.readUTF();
                synchronized (clients) {
                    clients.put(clientName, this);
                }
                
                logConnection(clientName, socket.getInetAddress().getHostAddress());

                sendMessage("Servidor", "Bem-vindo, " + clientName + "! Digite /users para listar usuários, /send [file/message] [usuário] [diretório/mensagem] para enviar mensagens ou /sair para sair.");

                String message;
                while ((message = input.readUTF()) != null) {
                    if (message.startsWith("/send message ")) {
                        handleTextMessage(message);
                    } else if (message.startsWith("/send file ")) {
                        handleFileTransfer(message);
                    } else if (message.equals("/users")) {
                        listUsers();
                    } else if (message.equals("/sair")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private void handleTextMessage(String message) {
            String[] parts = message.split(" ", 4);
            if (parts.length < 4) {
            	System.out.println("Faltam parâmetros para enviar a mensagem.");
            	return;
            }
            String recipient = parts[2];
            String msg = parts[3];

            if (clients.containsKey(recipient)) {
            	ClientHandler recipientHandler = clients.get(recipient);
                recipientHandler.sendMessage(clientName, msg);
            } else {
                sendMessage("Servidor", "Usuário " + recipient + " não está disponível.");
            }
        }

        private void handleFileTransfer(String message) throws IOException {
            String[] parts = message.split(" ", 4);
            if (parts.length < 4) {
                sendMessage("Servidor", "Formato incorreto. Use: /send file <destinatario> <caminho do arquivo>");
                return;
            }

            String recipient = parts[2];
            String filePath = parts[3];

            File file = new File(filePath);
            if (!file.exists()) {
                sendMessage("Servidor", "Arquivo não encontrado: " + filePath);
                return;
            }

            ClientHandler recipientHandler = clients.get(recipient);
            if (recipientHandler != null) {
                recipientHandler.sendMessage(clientName, "Enviando arquivo: " + file.getName());
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[(int) file.length()];
                    int bytesRead = fis.read(buffer);

                    if (bytesRead > 0) {
                        recipientHandler.output.writeUTF("/receivefile info-sending-file " + clientName + " info-sending-file " + file.getName() + " info-sending-file " + bytesRead);
                        recipientHandler.output.flush();

                        recipientHandler.output.write(buffer, 0, bytesRead);
                        recipientHandler.output.flush();
                    }
                }
            } else {
                sendMessage("Servidor", "Usuário " + recipient + " não está disponível.");
            }
        }
        
        private void listUsers() {
            StringBuilder userList = new StringBuilder("Usuários conectados: ");
            for (String user : clients.keySet()) {
                userList.append(user).append(", ");
            }
            sendMessage("Servidor", userList.toString());
        }

        private void sendMessage(String sender, String message) {
            try {
                output.writeUTF(sender + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disconnect() {
            try {
                synchronized (clients) {
                    clients.remove(clientName);
                }
                socket.close();
                System.out.println(clientName + " desconectado.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void logConnection(String clientName, String ip) {
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                String logEntry = String.format("%s - Cliente: %s, IP: %s%n",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), clientName, ip);
                fw.write(logEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
