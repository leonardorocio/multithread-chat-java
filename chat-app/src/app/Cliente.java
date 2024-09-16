package app;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente extends Thread {
    
    private final Socket socket;
    private static final Scanner SCANNER = new Scanner(System.in);
    private static boolean closed = false;

    public Cliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    // A Thread criada manualmente fica responsável por ler o que vier do servidor
    public void run() {
        try {
            Scanner input = new Scanner(this.socket.getInputStream());

            while (input.hasNextLine()) {
                String message = input.nextLine().trim();
                String[] splitMessage = message.split(" ");
                String action = splitMessage[0];
                if (action.equalsIgnoreCase("/send")) {
                    String inputType  = splitMessage[1];
                    String targetUser = splitMessage[2];

                    if (inputType.equalsIgnoreCase("file")) {
                        String fileName = splitMessage[3];
                        // Classe de leitura de arquivo
                    } else {
                        String text = splitMessage[3];
                        System.out.println(text);
                    }
                }

            }
            closed = true;
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readClientInput(Socket client) throws IOException {
        
        PrintStream output = new PrintStream(client.getOutputStream());
        while (!closed) {
            String message = SCANNER.nextLine().trim();
            String[] splitMessage = message.split(" ");
            String action = splitMessage[0];

            if (action.equalsIgnoreCase("/sair")) {
                closed = true;
                output.println(action); // Enviar mensagem pro servidor desconsiderar este cliente aqui
                output.close();
                client.close();
            } else if (action.equalsIgnoreCase("/send")) {
                
                String inputType  = splitMessage[1];
                String targetUser = splitMessage[2];

                if (inputType.equalsIgnoreCase("file")) {
                    String fileName = splitMessage[3];
                    // Classe de leitura de arquivo
                } else {
                    String text = splitMessage[3];
                    output.println(text);
                }
            } else if (action.equalsIgnoreCase("/users")) {
                output.println(action); // Mandar /users pro servidor e ele envia a resposta
            } else {
                System.out.println("Comando não existente!");
            }
            
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        Socket client = new Socket("127.0.0.1", 12345);
        Cliente serverInput = new Cliente(client);
        
        // Essa Thread aqui é responsável por receber os dados intermediados pelo servidor
        serverInput.start();
        readClientInput(client);

        SCANNER.close();
    }
}
