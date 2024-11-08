package game;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
//Client program
public class TicTacToeClient {
	
	//int whoseMove - player number whose move it is. Receives value from server. 
	//int winenr - stores the winner's player number (0 if no winner yet, 3 if draw). Receives value from server. 
	//int playNo - number of the client. 
	//String n - player name.
	//String playersMove - number of messages to send to the server, indicating player's move.
	//Format: player number-row to make the move in-column to make the move in-
	JMenuBar menu;
	JMenu control, help;
	JMenuItem exit, instructions;
	JLabel info;
	JFrame frame;
	JTextField name;
	JButton submit;
	JButton b00, b01, b02, b10, b11, b12, b20, b21, b22;
	JPanel grid;
	int whoseMove;
	int winner;
	int playNo;
	String n, playersMove;
	
	TicTacToeClient() {
		// Gui setup
		setupGui();
		// Other vars setup
		whoseMove = 1;
		winner = 0;
		playersMove = "";
		
	}
	
	void setupGui() {
		frame = new JFrame("Tic Tac Toe");
		menu = new JMenuBar();
		help = new JMenu("Aider");
		exit = new JMenuItem("Sortir");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		instructions = new JMenuItem("Reglez du jeu");
		instructions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "Le but du jeu est de réussir à aligner ses trois symboles, on remporte alors la partie.");
			}
		});
		
		help.add(instructions);
		help.add(exit);
		menu.add(help);
		//Info label setup
		info = new JLabel("Entrez votre nom");
		info.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.add(info);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(infoPanel);
		//Game board setup
		grid = new JPanel();
		grid.setSize(290, 350);
		b00 = new JButton();
		b01 = new JButton();
		b02 = new JButton();
		b10 = new JButton();
		b11 = new JButton();
		b12 = new JButton();
		b20 = new JButton();
		b21 = new JButton();
		b22 = new JButton();
		b00.setEnabled(false);
		b01.setEnabled(false);
		b02.setEnabled(false);
		b10.setEnabled(false);
		b11.setEnabled(false);
		b12.setEnabled(false);
		b20.setEnabled(false);
		b21.setEnabled(false);
		b22.setEnabled(false);
		grid.setLayout(new GridLayout(3,3));
		grid.add(b00);
		grid.add(b01);
		grid.add(b02);
		grid.add(b10);
		grid.add(b11);
		grid.add(b12);
		grid.add(b20);
		grid.add(b21);
		grid.add(b22);
		mainPanel.add(grid);
		//Bottom panel setup
		JPanel bottom = new JPanel();
		
		
		name = new JTextField(20);
		submit = new JButton("Soumettre");
		bottom.add(name);
		bottom.add(submit);
		bottom.setSize(bottom.getMinimumSize());
//		
		mainPanel.add(bottom);
		//Frame setup
		frame.setJMenuBar(menu);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 300);
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
	
	}
	
	// Sets up the game board using details (format: <values of 9 cells><whoseMove><winner>).
	void getBoard(String details) {
		if(details == null || details == "") {
			System.out.println("Empty");
			return;
		}
		changeButton(b00, details.charAt(0));
		changeButton(b01, details.charAt(1));
		changeButton(b02, details.charAt(2));
		changeButton(b10, details.charAt(3));
		changeButton(b11, details.charAt(4));
		changeButton(b12, details.charAt(5));
		changeButton(b20, details.charAt(6));
		changeButton(b21, details.charAt(7));
		changeButton(b22, details.charAt(8));
		whoseMove = Integer.parseInt(String.valueOf(details.charAt(9)));
		winner = Integer.parseInt(String.valueOf(details.charAt(10)));
	}
	
	void spectatorMode() {
		info.setVisible(false);
	}
	
	//Parameters: btn - button on which a move is performed.
	//	          char ch - character indicating which player made the move.
	//Description: Changes the button to the corresponding mark, if a move is valid. 
	void changeButton(JButton btn, char ch) {
		
		if(ch=='1') {
			btn.setFont(new Font("Arial", Font.BOLD, 36));
			btn.setText("X");
			if(playNo != 0) {
				btn.setForeground(Color.GREEN);
			} else {
				btn.setForeground(Color.BLACK);
			}
			btn.setEnabled(true);
		} else if(ch=='2') {
			btn.setFont(new Font("Arial", Font.BOLD, 36));
			btn.setText("O");
			if(playNo != 0) {
				btn.setForeground(Color.RED);
			} else {
				btn.setForeground(Color.BLACK);
			}
			btn.setEnabled(true);
		}
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TicTacToeClient tttc = new TicTacToeClient();
				Controller controller = new Controller(tttc);
				controller.start();
			}
		});
	}
	
	// Inner class which helps in connecting to the server and carrying out necessaary tasks. 
	public static class Controller {
		private Socket socket;
		private Scanner in;
		private PrintWriter out;
		private TicTacToeClient obj;
		
		Controller(TicTacToeClient t) {
			obj = t;
		}
		
		public void setSpectatorMode() {
			obj.info.setText("Spectator Mode");
			obj.name.setVisible(false);
			obj.submit.setVisible(false);
			
		}
		
		public void start() {
			try {
				this.socket = new Socket("127.0.0.1", 55277);
				this.in = new Scanner(socket.getInputStream());
				this.out = new PrintWriter(socket.getOutputStream(), true);
			} catch(UnknownHostException e) {
				System.out.println("error client 1");
				e.printStackTrace();
			} catch(IOException e) {
				System.out.println("error client 2");
				e.printStackTrace();
			}
			System.out.println("Initializing ClientHandler");
			Thread handler = new ClinetHandler(socket);
			System.out.println("Starting handler");
			
			 try {
				 	obj.playNo = Integer.parseInt(in.nextLine());
			    } catch (NumberFormatException e) {
			    	obj.playNo = 0;			    	
			    }
			 
			if(obj.playNo == 0) {
				setSpectatorMode();
				
			} 
			handler.start();

		}
		
		class ClinetHandler extends Thread {
			private Socket socket;
			
			public ClinetHandler(Socket socket) {
				this.socket = socket;
			}

			@Override
			public void run() {
				try {
				
					if(obj.playNo == 0) {
						readFromServer();
					} else {
						writeNameToServer();
						readFromServer();	
					}
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void writeNameToServer() throws Exception {
			try {
				obj.submit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(!obj.name.getText().contentEquals("")) {
							obj.n = obj.name.getText();
							obj.name.setEditable(false);
							obj.submit.setEnabled(false);
							obj.frame.setTitle("Tic Tac Toe - Player: "+obj.n);
							obj.info.setText("Welcome, "+obj.n+"!");
							obj.playersMove = obj.n;
							out.println(obj.playersMove);
							obj.playersMove = "";
							
						}
					}
				});
				writeCommandToServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void writeCommandToServer() throws Exception {
			try {
				obj.b00.addActionListener(new MoveListener());
				obj.b01.addActionListener(new MoveListener());
				obj.b02.addActionListener(new MoveListener());
				obj.b10.addActionListener(new MoveListener());
				obj.b11.addActionListener(new MoveListener());
				obj.b12.addActionListener(new MoveListener());
				obj.b20.addActionListener(new MoveListener());
				obj.b21.addActionListener(new MoveListener());
				obj.b22.addActionListener(new MoveListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		class MoveListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				obj.playersMove = "";
				System.out.println("Players executes a move");
				System.out.println(obj.playersMove);
				if(obj.whoseMove == obj.playNo) {
					obj.playersMove += Integer.toString(obj.playNo);
					if((JButton) e.getSource()==obj.b00) {
						obj.playersMove += "00";
					} else if((JButton) e.getSource()==obj.b01) {
						obj.playersMove += "01";
					} else if((JButton) e.getSource()==obj.b02) {
						obj.playersMove += "02";
					} else if((JButton) e.getSource()==obj.b10) {
						obj.playersMove += "10";
					} else if((JButton) e.getSource()==obj.b11) {
						obj.playersMove += "11";
					} else if((JButton) e.getSource()==obj.b12) {
						obj.playersMove += "12";
					} else if((JButton) e.getSource()==obj.b20) {
						obj.playersMove += "20";
					} else if((JButton) e.getSource()==obj.b21) {
						obj.playersMove += "21";
					} else if((JButton) e.getSource()==obj.b22) {
						obj.playersMove += "22";
					}
					out.println(obj.playersMove);
				}
				
			}
		}
		
		public void readFromServer() throws Exception {
			try {
				while(!in.hasNextLine()) {}
				while(in.hasNextLine()) {
					String command  = in.nextLine();
					if(command.contentEquals("Spectating")) {
//						obj.getBoard(command);
						obj.info.setText("You are spectator");
						obj.b00.setEnabled(true);
						obj.b01.setEnabled(true);
						obj.b02.setEnabled(true);
						obj.b10.setEnabled(true);
						obj.b11.setEnabled(true);
						obj.b12.setEnabled(true);
						obj.b20.setEnabled(true);
						obj.b21.setEnabled(true);
						obj.b22.setEnabled(true);
					}
					else if(command.contentEquals("Players ready.")) {
						obj.b00.setEnabled(true);
						obj.b01.setEnabled(true);
						obj.b02.setEnabled(true);
						obj.b10.setEnabled(true);
						obj.b11.setEnabled(true);
						obj.b12.setEnabled(true);
						obj.b20.setEnabled(true);
						obj.b21.setEnabled(true);
						obj.b22.setEnabled(true);
					}
					else if(command.equals("Game ends. The other player left.")) {
						JOptionPane.showMessageDialog(obj.frame, command);
						socket.close();
						System.exit(0);
					} else {
						obj.getBoard(command);
						
						if(obj.playNo==obj.whoseMove) {
							obj.info.setText("À ton tour. Veuillez faire un geste.");
							writeCommandToServer();
						} else if(obj.playNo == 0) {
							obj.info.setText("Vous êtes spectateur");
						} else {
							obj.info.setText("Au tour de l'adversaire. Attendez, s'il vous plaît.");
						}
						if(obj.winner!=0) {
							if(obj.winner==3) {
								JOptionPane.showMessageDialog(obj.frame, "Bien joué, c'est un match nul !");
							} else if(obj.winner==obj.playNo) {
								JOptionPane.showMessageDialog(obj.frame, "Félicitations, vous avez gagné!");
							} else if(0==obj.playNo) {
								JOptionPane.showMessageDialog(obj.frame, "Jeu Termine!");
							} else {
								JOptionPane.showMessageDialog(obj.frame, "Vous avez perdu! :(");
							}
							System.exit(0);
						}
					}
					while(!in.hasNextLine()) {}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}
	}
}