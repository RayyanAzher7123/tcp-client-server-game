package tcpgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Listens on localhost:50000 and plays one guessing game per connection.
 *
 * One client is served at a time. When that client wins, quits, or drops,
 * the socket is closed and the server waits for the next connection.
 *
 * Optional demo mode: {@code java tcpgame.GuessServer 63} uses 63 for every
 * game. With no argument, each game gets a new number from 1 to 100.
 */
public class GuessServer {

    public static void main(String[] args) {
        final Integer fixedSecret;
        try {
            fixedSecret = secretFromArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: java tcpgame.GuessServer [secret]");
            System.exit(1);
            return;
        }

        Random random = new Random();

        try (ServerSocket serverSocket = new ServerSocket(
                Protocol.PORT, 50, InetAddress.getByName(Protocol.HOST))) {
            System.out.println("Guess server listening on localhost:" + Protocol.PORT);
            if (fixedSecret != null) {
                System.out.println("Demo mode: every game uses " + fixedSecret + ".");
            }
            System.out.println("Waiting for a client. Press Ctrl+C to stop.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                    // Closing the listening socket unblocks accept().
                }
            }));

            while (true) {
                try (Socket client = serverSocket.accept()) {
                    client.setTcpNoDelay(true);
                    int secret = fixedSecret != null
                            ? fixedSecret
                            : random.nextInt(Protocol.MAX - Protocol.MIN + 1) + Protocol.MIN;
                    play(client, secret);
                } catch (SocketException e) {
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    System.err.println("Connection error: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            }
        } catch (BindException e) {
            System.err.println("Port " + Protocol.PORT
                    + " is already in use. Stop the other server and try again.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server failed: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Server stopped.");
    }

    /**
     * @return a fixed secret, or null when each game should pick at random
     */
    private static Integer secretFromArgs(String[] args) {
        if (args.length == 0) {
            return null;
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Too many arguments.");
        }
        try {
            int secret = Integer.parseInt(args[0]);
            if (secret < Protocol.MIN || secret > Protocol.MAX) {
                throw new IllegalArgumentException(
                        "Secret must be between " + Protocol.MIN + " and " + Protocol.MAX + ".");
            }
            return secret;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Secret must be a whole number.");
        }
    }

    private static void play(Socket client, int secret) throws IOException {
        System.out.println("Client connected: " + client.getRemoteSocketAddress());
        System.out.println("Secret number is " + secret + ".");

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        client.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                String response = respond(line, secret);
                System.out.println("  \"" + line.trim() + "\" -> " + response);
                out.println(response);
                if (Protocol.CORRECT.equals(response) || Protocol.GOODBYE.equals(response)) {
                    System.out.println("Closing connection.");
                    return;
                }
            }
            System.out.println("Client disconnected. Closing connection.");
        }
    }

    static String respond(String line, int secret) {
        String text = line.trim();
        if (text.equalsIgnoreCase(Protocol.QUIT)) {
            return Protocol.GOODBYE;
        }

        int guess;
        try {
            guess = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return Protocol.INVALID;
        }
        if (guess < Protocol.MIN || guess > Protocol.MAX) {
            return Protocol.INVALID;
        }
        if (guess < secret) {
            return Protocol.TOO_LOW;
        }
        if (guess > secret) {
            return Protocol.TOO_HIGH;
        }
        return Protocol.CORRECT;
    }
}
