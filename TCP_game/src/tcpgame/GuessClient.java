package tcpgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Connects to the guess server, sends each guess, and prints the reply.
 *
 * Type a whole number from 1 to 100, or {@code quit} to close the connection.
 */
public class GuessClient {

    public static void main(String[] args) {
        System.exit(run());
    }

    private static int run() {
        System.out.println("Connecting to localhost:" + Protocol.PORT + "...");

        try (Socket socket = new Socket(Protocol.HOST, Protocol.PORT)) {
            socket.setTcpNoDelay(true);
            return play(socket);
        } catch (ConnectException e) {
            System.err.println("Could not connect to localhost:" + Protocol.PORT
                    + ". Start the server first.");
            return 1;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return 1;
        }
    }

    private static int play(Socket socket) throws IOException {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8), true);
                Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)
        ) {
            System.out.println("Connected. Guess a number between "
                    + Protocol.MIN + " and " + Protocol.MAX + ".");
            System.out.println("Type quit to exit.");

            int attempts = 0;
            while (true) {
                System.out.print("Enter guess: ");
                System.out.flush();

                if (!scanner.hasNextLine()) {
                    out.println(Protocol.QUIT);
                    out.checkError();
                    System.out.println();
                    System.out.println("Closing connection.");
                    return 0;
                }

                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("quit")) {
                    out.println(Protocol.QUIT);
                    if (out.checkError()) {
                        System.err.println("Lost connection to the server.");
                        return 1;
                    }
                    String reply = in.readLine();
                    if (reply == null) {
                        System.err.println("Server closed the connection.");
                    }
                    System.out.println("Closing connection.");
                    return 0;
                }

                if (!isWholeNumberInRange(line)) {
                    System.out.println("Please enter a whole number between "
                            + Protocol.MIN + " and " + Protocol.MAX + ".");
                    continue;
                }

                out.println(line);
                if (out.checkError()) {
                    System.err.println("Lost connection to the server.");
                    return 1;
                }

                String reply = in.readLine();
                if (reply == null) {
                    System.err.println("Server closed the connection.");
                    return 1;
                }

                switch (reply) {
                    case Protocol.TOO_LOW -> {
                        attempts++;
                        System.out.println("Too low! Try again.");
                    }
                    case Protocol.TOO_HIGH -> {
                        attempts++;
                        System.out.println("Too high! Try again.");
                    }
                    case Protocol.CORRECT -> {
                        attempts++;
                        String noun = attempts == 1 ? "attempt" : "attempts";
                        System.out.println("Correct! You guessed the number in "
                                + attempts + " " + noun + ".");
                        return 0;
                    }
                    case Protocol.INVALID -> System.out.println(
                            "Server rejected that guess. Enter a whole number between "
                                    + Protocol.MIN + " and " + Protocol.MAX + ".");
                    default -> System.out.println("Unexpected response: " + reply);
                }
            }
        }
    }

    private static boolean isWholeNumberInRange(String text) {
        try {
            int guess = Integer.parseInt(text);
            return guess >= Protocol.MIN && guess <= Protocol.MAX;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
