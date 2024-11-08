package game;

import java.util.*;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;
//Server class
public class TicTacToeServer {
	
	//int whoseMove - stores the player number of the next move
	//int winner - stores the winner
	//int numOfPlayers - stores the number of players
	//int moveCounter - stores the total number of moves
	//int board[][] - 3x3 array storing the game board - 0's indicate empty cells, 1's indicate X's and 2's indicate O's
	//String result - stores the message to broadcast to the clients
	int whoseMove, winner, numOfPlayers, moveCounter;
	int board[][];
	String result;
	private ServerSocket serverSocket;
	private Set<PrintWriter> writers = new HashSet<>();
	
	// Initializes instance variables
	TicTacToeServer(ServerSocket serverSocket) throws IOException{
		whoseMove = 1;
		winner = 0;
		numOfPlayers = 0;
		moveCounter = 0;
		board = new int[3][3];
		for(int i=0; i<3; i++) {
			for(int j=0;j<3; j++) {
				board[i][j] = 0;
			}
		}
		this.serverSocket = serverSocket;
		result = "";
	}
	
	//Method name: makeMove()
	//Parameter: String str 
	//Description: Takes in str which is in the format - <playerNumber><row><column>.
	//	       Checks if the particular move is valid, and makes it.
	//    	       Calls sendToClients() to update the broadcast message (to be sent to the clients).
	void makeMove(String str, boolean spectator) {
		if(numOfPlayers>=2) {
			int whichPlayer = Integer.parseInt(String.valueOf(str.charAt(0)));
			int row = Integer.parseInt(String.valueOf(str.charAt(1)));
			int col = Integer.parseInt(String.valueOf(str.charAt(2)));
			if(whichPlayer == whoseMove && board[row][col]==0) {
				board[row][col] = whichPlayer;
				if(!spectator) {
					moveCounter++;
					togglePlayer();	
				}
			}
			result = sendToClients();
		}
	}
	
	//Method name: togglePlayer()
	//Description: Changes the player whose move it is, once a move has been made. 
	void togglePlayer() {
		if(whoseMove == 1) {
			whoseMove = 2;
		} else if(whoseMove == 2) {
			whoseMove = 1;
		}
	}
	
	//Method name: checkForWinner()
	//Description: Calls checkRows(), checkColumns() and checkDiagonals() to determine the winner (or a draw).
	//             If player one wins, winner = 1.
	//	       If player two wins, winner = 2.
	//	       If it's a draw, winner = 3.
	void checkForWinner() {
		checkRows();
		checkColumns();
		checkDiagonals();
		if(winner==0 && moveCounter==9) {
			winner = 3;
		}
	}
	
	void checkRows() {
		if(board[0][0]==board[0][1] && board[0][1]==board[0][2]) {
			if(board[0][0]==1) {
				winner = 1;
			} else if(board[0][0]==2) {
				winner = 2;
			}
		}
		else if(board[1][0]==board[1][1] && board[1][1]==board[1][2]) {
			if(board[1][0]==1) {
				winner = 1;
			} else if(board[1][0]==2) {
				winner = 2;
			}
		}
		else if(board[2][0]==board[2][1] && board[2][1]==board[2][2]) {
			if(board[2][0]==1) {
				winner = 1;
			} else if(board[2][0]==2) {
				winner = 2;
			}
		}
	}
	
	void checkColumns() {
		if(board[0][0]==board[1][0] && board[1][0]==board[2][0]) {
			if(board[0][0]==1) {
				winner = 1;
			} else if(board[0][0]==2) {
				winner = 2;
			}
		}
		else if(board[0][1]==board[1][1] && board[1][1]==board[2][1]) {
			if(board[0][1]==1) {
				winner = 1;
			} else if(board[0][1]==2) {
				winner = 2;
			}
		}
		else if(board[0][2]==board[1][2] && board[1][2]==board[2][2]) {
			if(board[0][2]==1) {
				winner = 1;
			} else if(board[0][2]==2) {
				winner = 2;
			}
		}
	}
	
	//Method name: checkDiagonals()
	//Description: Checks the diagonals of the board to see if there's a winner. Updates winner accordingly.
	void checkDiagonals() {
		if(board[1][1]==board[0][0] && board[1][1]==board[2][2]) {
			if(board[1][1]==1) {
				winner = 1;
			} else if(board[1][1]==2) {
				winner = 2;
			}
		}
		else  if(board[1][1]==board[0][2] && board[1][1]==board[2][0]) {
			if(board[1][1]==1) {
				winner = 1;
			} else if(board[1][1]==2) {
				winner = 2;
			}
		}
	}
	
	//Method name: reset()
	//Description: Resets the instance variable values. 
	void reset() {
		whoseMove = 1;
		winner = 0;
		numOfPlayers = 0;
		moveCounter = 0;
		result = "";
		for(int i=0; i<3; i++) {
			for(int j=0;j<3; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	// Returns a string which will be broadcasted to the clients.
	// String format is: <values of the 9 board cells - 0 for empty/1 for X/2 for O><whoseMove><winner>
	String sendToClients() {
		String str = "";
		for(int i=0; i<3;i++) {
			for(int j=0; j<3; j++) {
				str += Integer.toString(board[i][j]);
			}
		}
		str += Integer.toString(whoseMove);
		checkForWinner();
		str += Integer.toString(winner);
		return str;
	}
	
	public static void main(String args[]) {
		System.out.println("Server is running...");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Server stopped.");
			}
		}));
		
		try (var listener = new ServerSocket(55277)) {
			TicTacToeServer myServer = new TicTacToeServer(listener);
			System.out.println("Starting server.");
			myServer.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	//Method name: start()
	//Description: Sets up the server and starts required threads after connecting with the clients. 
	public void start() {
		var pool = Executors.newFixedThreadPool(200);
		int clientCount = 0;
		while (winner == 0) {
			try {
				Socket s1 = serverSocket.accept();
				System.out.println("Connected to client " + ++clientCount);
				PrintWriter output1 = new PrintWriter(s1.getOutputStream(), true);
				// Index of first player playing
				output1.println("1");
				
				Socket s2 = serverSocket.accept();
				System.out.println("Connected to client " + ++clientCount);
				PrintWriter output2 = new PrintWriter(s2.getOutputStream(), true);
				// Index of second player playing
				output2.println("2");
				
				System.out.println("Getting input stream 1");
				Scanner input1 = new Scanner(s1.getInputStream());
				System.out.println("Getting input stream 2");
				Scanner input2 = new Scanner(s2.getInputStream());
				System.out.println("While input 1 has next line");
				while(!input1.hasNextLine()) {}
				numOfPlayers++;
				System.out.println("Input 1 temp");
				String temp = input1.nextLine();
				System.out.println("Input 2 has next line");
				while(!input2.hasNextLine()) {}
				numOfPlayers++;
				System.out.println("Input 2 next line");
				temp = input2.nextLine();
				System.out.println("Writers");
				writers.add(output1);
				writers.add(output2);
				output1.println("Players ready.");
				output2.println("Players ready.");
				pool.execute(new Handler(s1, output1, input1));
				pool.execute(new Handler(s2, output2, input2));	
				// Accept guests
				while(winner == 0) {
					try {
						Socket tempSocket = serverSocket.accept();
						PrintWriter outputTemp = new PrintWriter(tempSocket.getOutputStream(), true);
						outputTemp.println("0");
						writers.add(outputTemp);
						Scanner input3 = new Scanner(tempSocket.getInputStream());
						pool.execute(new Handler(tempSocket, outputTemp,input3 ));

					} catch (IOException e) {
						System.out.println("Problem");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String lastCommand = "";

	//Inner class to handle clients. 
	public class HandlerWatch implements Runnable {
		private Socket socket;
		private Scanner input;
		private PrintWriter output;
		
		
		public HandlerWatch(Socket socket, PrintWriter output, Scanner input) {
			this.socket = socket;
			this.output = output;
			this.input = input;
			System.out.println("Initializing Watcher for Spectator");
			if(lastCommand != "") {
				makeMove(lastCommand, true);
				for(PrintWriter writer : writers) {
					writer.println(result);
				}
			}
		}
		
		
		
		@Override
		public void run() {
			System.out.println("Connected: "+socket);
			try {
				if(lastCommand != "") {
					makeMove(lastCommand, true);
					for(PrintWriter writer : writers) {
						writer.println(result);
					}
				}
				
				
				while(input.hasNextLine()) {
					numOfPlayers = 2;
					String command = input.nextLine();
					makeMove(command, true);
					for(PrintWriter writer : writers) {
						writer.println(result);
					}
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} 
		}
	}

	//Inner class to handle clients. 
	public class Handler implements Runnable {
		private Socket socket;
		private Scanner input;
		private PrintWriter output;
		
		
		public Handler(Socket socket, PrintWriter output, Scanner input) {
			this.socket = socket;
			this.output = output;
			this.input = input;
		}
		
		
		
		@Override
		public void run() {
			System.out.println("Connected: "+socket);
			try {
				
				while(input.hasNextLine()) {
					numOfPlayers = 100;
					String command = input.nextLine();
					makeMove(command, false);
					lastCommand = command;					
					for(PrintWriter writer : writers) {
						writer.println(result);
					}
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
			} finally {
				if(output == null) {
					return;
				}
				if(output != null) {
					writers.remove(output);
					for(PrintWriter writer : writers) {
						writer.println("Game ends. The other player left.");
					}
					numOfPlayers--;
					System.out.println("Player left.");
					reset();
				}
			}
		}
	}
}