package clientserverarch.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import clientserverarch.constants.Constants;

public class Server {
	private int listenPort;
	private ServerSocket serverSocket;
	private boolean isStarted;
	private UsersLog usersLog;           /*list of all users*/
	private Dispatcher dispatcher;

	public Server(int port) {
		listenPort = port;
		usersLog = new UsersLog();
		dispatcher = new Dispatcher(this, usersLog);
		
		try {
			serverSocket = new ServerSocket(listenPort);
			isStarted = true;
			System.out.println("Server started on port: " + listenPort);
		} catch (IOException iOEx) {
			System.err.println("Cannot start server!");
			iOEx.printStackTrace();
		}
	}

	public Server() {
		this(Constants.DEFAULT_PORT); //reuse
	}
	
	public boolean getIsServerStarted() {
		return isStarted;
	}

	public void start() {
		new Thread(dispatcher).start();
		
		while (isStarted) {
			try {
				Socket socket = serverSocket.accept();
				ClientSender clientSender = new ClientSender(socket, dispatcher);
				new Thread(clientSender).start();
				ClientListener clientListener = new ClientListener(socket, usersLog, this, 
						dispatcher, clientSender);
				new Thread(clientListener).start();
			} catch (IOException iOEx) {
				System.err.println("Server stopped!");
				iOEx.printStackTrace();
			} 
		}
	}

	public void stop() {
		isStarted = false;
		try {
			serverSocket.close();
		} catch (IOException iOEx) {
			System.err.println("Could NOT stop ServerSocket!");
			iOEx.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		Server chatServer = new Server(Constants.PORT);
		chatServer.start();
	}
}
