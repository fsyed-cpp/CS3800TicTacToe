import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * A server for a multi-player tic tac toe game. Loosely based on an example in
 * Deitel and Deitel’s “Java How to Program” book. For this project I created a
 * new application-level protocol called TTTP (for Tic Tac Toe Protocol), which
 * is entirely plain text. The messages of TTTP are:
 *
 * Client -> Server MOVE <n> QUIT
 *
 * Server -> Client WELCOME <char> VALID_MOVE OTHER_PLAYER_MOVED <n>
 * OTHER_PLAYER_LEFT VICTORY DEFEAT TIE MESSAGE <text>
 */
public class TicTacToeServer {

    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(58901)) {
            System.out.println("Tic Tac Toe Server is Running...");
            var pool = Executors.newFixedThreadPool(200);
            while (true) {
                Game game = new Game();
                pool.execute(game.new Player(listener.accept(), 'X'));
                pool.execute(game.new Player(listener.accept(), 'O'));
            }
        }
    }
}

class Game {

    // Board cells numbered 0-8, top to bottom, left to right; null if empty
    private Player[] board = new Player[9];

    Player currentPlayer;

    public boolean hasWinner() {
        return (board[0] != null && board[0] == board[1] && board[0] == board[2])
                || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                || (board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    public boolean boardFilledUp() {
        return Arrays.stream(board).allMatch(p -> p != null);
    }

    public synchronized void move(int location, Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[location] != null) {
            throw new IllegalStateException("Cell already occupied");
        }
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }

    /**
     * A Player is identified by a character mark which is either 'X' or 'O'. For
     * communication with the client the player has a socket and associated Scanner
     * and PrintWriter.
     */
    class Player implements Runnable {
        char mark;
        Player opponent;
        Socket socket;
        DataInputStream input;
        DataOutputStream output;

        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
        }

        @Override
        public void run() {
            try {
                setup();
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    try {
                        opponent.output.writeUTF("OTHER_PLAYER_LEFT");
                        opponent.output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void setup() throws IOException {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF("WELCOME " + mark);
            output.flush();
            if (mark == 'X') {
                currentPlayer = this;
                output.writeUTF("WAITING Waiting for opponent to connect");
                output.flush();
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.writeUTF("MESSAGE Your move");
                output.flush();
            }
        }

        private void processCommands() throws IOException {
            while (true) {
                var command = input.readUTF();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.substring(5)));
                }
            }
        }

        private void processMoveCommand(int location) throws IOException {
            try {
                move(location, this);
                output.writeUTF("VALID_MOVE");
                output.flush();
                if (hasWinner()) {
                    output.writeUTF("VICTORY");
                    output.flush();
                    opponent.output.writeUTF("OPPONENT_MOVED_END " + location);
                    opponent.output.flush();
                    opponent.output.writeUTF("DEFEAT");
                    opponent.output.flush();
                } else if (boardFilledUp()) {
                    output.writeUTF("TIE");
                    output.flush();
                    opponent.output.writeUTF("TIE");
                    opponent.output.flush();
                }
                opponent.output.writeUTF("OPPONENT_MOVED " + location);
                opponent.output.flush();
            } catch (IllegalStateException e) {
                output.writeUTF("MESSAGE " + e.getMessage());
                output.flush();
            }
        }

    }
}