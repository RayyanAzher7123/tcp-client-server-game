package tcpgame;

/**
 * Line-based text protocol for the guessing game.
 *
 * Each message is one UTF-8 line. The client sends a guess or QUIT.
 * The server answers with one status word.
 */
public final class Protocol {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 50000;

    public static final int MIN = 1;
    public static final int MAX = 100;

    public static final String TOO_LOW = "TOO_LOW";
    public static final String TOO_HIGH = "TOO_HIGH";
    public static final String CORRECT = "CORRECT";
    public static final String INVALID = "INVALID";
    public static final String QUIT = "QUIT";
    public static final String GOODBYE = "GOODBYE";

    private Protocol() {
    }
}
