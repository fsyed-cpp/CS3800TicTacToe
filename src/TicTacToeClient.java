import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

//Basic tic tac toe game in which two players can connect through a server and play simultaneously.
//Using an application-level protocol called TTTP (for Tic Tac Toe Protocol). Input and output are show in command line from the clients' side.

public class TicTacToeClient {

    private static String[] board = { "0", "1", "2", "3", "4", "5", "6", "7", "8" }; //initializing board
    private int myNum; //to keep track of previous move. Updates board in correct item in the array when a valid move is given by the player.
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

    
    
    //Client will listen for server messages. Specific keywords include WELCOME, VICTORY, DEFEAT, OTHER_PLAYER_LEFT
    //this messages will trigger specific actions for the client side when received from the server.
    public void play() throws Exception {
        try {
            String response = in.readUTF();
            System.out.println(response);
            var mark = response.charAt(8);
            var opponentMark = mark == 'X' ? 'O' : 'X';
            while (true) {
                response = in.readUTF();
                if (response.startsWith("VALID_MOVE")) { //if valid move
                    board[myNum] = Character.toString(mark); //updated board with correct mark
                    printGrid(); //show grid to client
                    System.out.println("Valid move, please wait");
                } else if (response.startsWith("OPPONENT_MOVED_END")) {
                    var loc = Integer.parseInt(response.substring(19));
                    board[loc] = Character.toString(opponentMark); //update board with opponent's mark
                    printGrid(); //show grid to client when opponent makes a move.
                    System.out.println("Opponent moved " + loc);
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    var loc = Integer.parseInt(response.substring(15));
                    board[loc] = Character.toString(opponentMark);
                    printGrid();
                    System.out.println("Opponent moved " + loc + ", your turn");
                    String num = "";
                    boolean isValid = false; //for input validation
                    do {
                        try {
                            System.out.print("MOVE ");
                            num = br.readLine();
                            myNum = Integer.parseInt(num);
                            isValid = true; // Sets to true if num is integer, otherwise, go to 'catch' block
                        } catch (Exception e1) {
                            System.out.println("Invalid move");
                        }
                        if (myNum < 0 || myNum > 8) {
                            System.out.println("Invalid move," + num + " is not within 0-8"); //show error. User needs to try again.
                        }
                    } while ((myNum < 0 || myNum > 8) || isValid == false);
                    out.writeUTF("MOVE " + num);
                } else if (response.startsWith("WAITING")) {
                    System.out.println(response.substring(8));
                } else if (response.startsWith("MESSAGE")) {
                    System.out.println(response.substring(8));
                    String num = "";
                    boolean isValid = false;
                    do {
                        try {
                            System.out.print("MOVE "); //asking for user input
                            num = br.readLine();
                            myNum = Integer.parseInt(num);
                            isValid = true;
                        } catch (Exception e1) { //input validation
                            System.out.println("Invalid move");
                        }
                        if (myNum < 0 || myNum > 8) {
                            System.out.println("Invalid move," + num + " is not within 0-8");
                        }
                    } while ((myNum < 0 || myNum > 8) || isValid == false);
                    out.writeUTF("MOVE " + num);
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
            System.out.println("QUIT"); //show game is over. 
        } catch (Exception e) {
            System.out.println("Disconnected from Tic Tac Toe Server");
        } finally {
            socket.close(); //disconnect from server.
            // frame.dispose();
        }
    }

    private static void printGrid() { //printing grid

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
        try {
            TicTacToeClient client = new TicTacToeClient("localhost");
            printGrid(); //show empty grid
            client.play(); //initialize game
        } catch (Exception e) {
            System.out.println("Unable to connect to Tic Tac Toe Server"); //catch error of not being able to connect.
        }
    }
}
