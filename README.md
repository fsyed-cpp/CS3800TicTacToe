# CS3800TicTacToe

User Manual:

- To run the project: (This manual will assume the user is using IntelliJ or any IDE that supports Java and the Socket Framework)

1) Run one instance of the TicTacoToeServer.java file
2) Run two separate instances of the TicTacoToeClient.java file
3) One of the clients will say: "WELCOME (X/O)" and then "WAITING FOR OPPONENT"
4) Once you see the "MOVE" output in the console, you may enter any input from 1-9.
	- Assume 1-3 is in order :(top left, top middle, top right)
	- Assume 4-6 in order is: (left middle, middle middle, middle right)
	- Assume 7-9 in order is: (bottom left, bottom middle, bottom right)
5) Once you input your move, the console will either tell you your input is valid or invalid
6) If your input is valid, the console will tell you so and to please wait for the other client to make a move.
7) After the opponent moves, you will see "Opponent moved x, your turn", where "x" is the number space that the opponent moved.

8) The game will continue until there is a winner in which the console will declare so, or until all the spaces are filled in which it will declare a tie.
