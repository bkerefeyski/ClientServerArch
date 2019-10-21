package clientserverarch.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import clientserverarch.constants.Constants;

public class Client {
	private Socket socket;
	private String serverHost;
	private int serverPort;
	private PrintWriter socketWriter;
	private PrintWriter consoleWriter;
	private BufferedReader socketReader;
	private BufferedReader consoleReader;
	private boolean isStarted;
	
	public Client(String host, int port) {
		serverHost = host;
		serverPort = port;
			
		try {
			socket = new Socket(serverHost, serverPort);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream());
			consoleReader = new BufferedReader(new InputStreamReader(System.in));
			consoleWriter = new PrintWriter(System.out);
			isStarted = true;
			System.out.println("Client started.");
		} catch (UnknownHostException unknownHostEx) {
			System.err.println("Host problem - Unknown Host.");
			unknownHostEx.printStackTrace();
		} catch (IOException iOEx) {
			System.err.println("Can NOT connect server!");
			iOEx.printStackTrace();
			close();
		}
	}
	
	public Socket getSocket() {
		return socket;
	}

	public void start() {
		if (isStarted) {
			int authentication = logIn();
			
			switch (authentication) {
				case -1: 
					System.out.println("User already online/logged in!");
					close();
					break;
				case 0: 			/*correct user and pass*/
					/*falls through*/
				case 1: 			/*new user created*/
					System.out.println("Log in successful!");
					
					/*Actual start of the client`s threads*/
					MessageTransmitter consoleToSocket = new MessageTransmitter (this, consoleReader, 
							socketWriter, true);
					new Thread(consoleToSocket).start();
					MessageTransmitter serverToConsole = new MessageTransmitter (this, socketReader, 
							consoleWriter, false);
					new Thread (serverToConsole).start();
					break;
				case 2: 
					System.out.println("Wrong Password!");
					close();
					break;
				default: 
					System.out.println("Something went wrong - please try again!");
					close();
				}
		} else {
			close();
			System.err.println("Client cannot start!");
			
		}
	}
	
	/**
	 * Method logIn gets user and pass input from the client 
	 * and validates them on the server. 
	 * 
	 * @return        0 or 1 when login is successful
	 *                -2, -1 or 2 when login is not successful
	 */
	public int logIn() {
		String user = null;
		String pass = null;
		int returnValue = -2;
		
		try {
			while (user == null || user.length() < Constants.USER_MIN_LENGHT 
					|| user.length() > Constants.USER_MAX_LENGHT) {
				System.out.print("Enter User name:");
				user = consoleReader.readLine();
			}
			while (true) {
				System.out.print("Enter Password:");
				pass = consoleReader.readLine();
				
				/* Uses regex for password pattern validation.
				 * Check class Constants for more details on the regex.
				 */
				if (pass.matches(Constants.PASSWORD_PATTERN)) {
					break;
				}
			}
		} catch (IOException iOEx) {
			iOEx.printStackTrace();
		}
		socketWriter.println(user);
		socketWriter.println(pass);
		socketWriter.flush();
		
		try {
			String value = socketReader.readLine();
			returnValue = Integer.parseInt(value);
		} catch (IOException iOEx) {
			iOEx.printStackTrace();
		}
		return returnValue;
	}

	public void close() {
		try {
			consoleWriter.close();
			consoleReader.close();
			socketWriter.close();
			socketReader.close();
			socket.close();
		} catch (IOException iOEx) {
			iOEx.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final String LOCAL_HOST = "localhost";
		Client chatClient = new Client(LOCAL_HOST, Constants.PORT);
		chatClient.start();
	}
}
