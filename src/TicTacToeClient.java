import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A client for a multi-player tic tac toe game. Loosely based on an example in
 * Deitel and Deitel’s “Java How to Program” book. For this project I created a
 * new application-level protocol called TTTP (for Tic Tac Toe Protocol), which
 * is entirely plain text. The messages of TTTP are:
 *
 * Client -> Server MOVE <n> QUIT
 *
 * Server -> Client WELCOME <char> VALID_MOVE OTHER_PLAYER_MOVED <n>
 * OTHER_PLAYER_LEFT VICTORY DEFEAT TIE MESSAGE <text>
 */
public class TicTacToeClient {

    private static String[] board = { "0", "1", "2", "3", "4", "5", "6", "7", "8" };
    private int myNum;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader br;

    public TicTacToeClient(String serverAddress) throws Exception {

        socket = new Socket(serverAddress, 58901);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * The main thread of the client will listen for messages from the server. The
     * first message will be a "WELCOME" message in which we receive our mark. Then
     * we go into a loop listening for any of the other messages, and handling each
     * message appropriately. The "VICTORY", "DEFEAT", "TIE", and
     * "OTHER_PLAYER_LEFT" messages will ask the user whether or not to play another
     * game. If the answer is no, the loop is exited and the server is sent a "QUIT"
     * message.
     */
    public void play() throws Exception {
        try {
            String response = in.readUTF();
            System.out.println(response);
            var mark = response.charAt(8);
            var opponentMark = mark == 'X' ? 'O' : 'X';
            while (true) {
                response = in.readUTF();
                if (response.startsWith("VALID_MOVE")) {
                    board[myNum] = Character.toString(mark);
                    printGrid();
                    System.out.println("Valid move, please wait");
                } else if (response.startsWith("OPPONENT_MOVED_END")) {
                    var loc = Integer.parseInt(response.substring(19));
                    board[loc] = Character.toString(opponentMark);
                    System.out.println("Opponent moved " + loc);
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    var loc = Integer.parseInt(response.substring(15));
                    board[loc] = Character.toString(opponentMark);
                    printGrid();
                    System.out.println("Opponent moved " + loc + ", your turn");
                    System.out.print("MOVE ");
                    String num = br.readLine();
                    try {
                        out.writeUTF("MOVE " + num);
                        myNum = Integer.parseInt(num);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else if (response.startsWith("WAITING")) {
                    System.out.println(response.substring(8));
                } else if (response.startsWith("MESSAGE")) {
                    System.out.println(response.substring(8));
                    System.out.print("MOVE ");
                    String num = br.readLine();
                    try {
                        out.writeUTF("MOVE " + num);
                        myNum = Integer.parseInt(num);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else if (response.startsWith("VICTORY")) {
                    System.out.println("Winner Winner");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    System.out.println("Sorry you lost");
                    break;
                } else if (response.startsWith("TIE")) {
                    System.out.println("Tie");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    System.out.println("Other player left");
                    break;
                }

            }
            System.out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            // frame.dispose();
        }
    }

    private static void printGrid() {

        System.out.println("UPDATED BOARD:\n");
        System.out.println(board[0] + " | " + board[1] + " | " + board[2]);
        System.out.println("\n----------");
        System.out.println(board[3] + " | " + board[4] + " | " + board[5]);
        System.out.println("\n----------");
        System.out.println(board[6] + " | " + board[7] + " | " + board[8]);
        System.out.println("\n\n");
    }

    public static void main(String[] args) throws Exception {
        /*
         * if (args.length != 1) {
         * System.err.println("Pass the server IP as the sole command line argument");
         * return;
         * }
         */
        TicTacToeClient client = new TicTacToeClient("localhost");
        printGrid();
        client.play();
    }
}