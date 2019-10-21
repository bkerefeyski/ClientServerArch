package clientserverarch.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import clientserverarch.constants.Constants;

public class ClientListener implements Runnable {
	private boolean isStart;
	private boolean isThreadOn;
	private boolean hasClientClose;
	private UsersLog usersLog;
	private User currUser;                   /*Identify the thread with a user*/
	private Server server;                   /*To be used when stopping the server*/
	private Dispatcher dispatcher;
	private ClientSender currClientSender;
	private Socket currentSocket;
	private BufferedReader currSocketRead;
	private PrintWriter currSocketWrite;
	
	public ClientListener(Socket currentSocket, UsersLog usersLog, Server server, 
				Dispatcher dispatcher, ClientSender currClientSender) {
		this.currentSocket = currentSocket;
		this.usersLog = usersLog;
		this.server = server;
		this.dispatcher = dispatcher;
		this.currClientSender = currClientSender;
		try {
			this.currentSocket.setSoTimeout(Constants.CLIENT_REQUEST_TIMEOUT);
			currSocketRead = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
			currSocketWrite = new PrintWriter(currentSocket.getOutputStream());
			isStart = true;
		} catch (SocketException socketE) {
			socketE.printStackTrace();
		} catch (IOException iOEx) {
			iOEx.printStackTrace();
		}
	}
	
	/**
	 * Checks user authentication and if verified starts the thread.
	 * Obtains user and pass from the client. 
	 * Checks user authentication and returns value to the client.
	 * Starts the tread if user is verified.
	 */
	@Override
	public void run(){
		if (isStart) {
			String user = null;
			String pass = null;
			try {
				user = currSocketRead.readLine();
				pass = currSocketRead.readLine();
			} catch (IOException iOEx) {
				iOEx.printStackTrace();
			}
			
			/*Verifies the user and pass and sends the result back to the client*/
			int authentication = usersLog.logIn(user, pass, dispatcher);
			currSocketWrite.println(authentication);
			currSocketWrite.flush();
			
			/*If log in is successful identify the thread with a user and add the user to online users list*/
			if ((authentication == 0) || (authentication == 1)) {
				isThreadOn = true;
				String ipAddress = currentSocket.getInetAddress().getHostAddress();
				currUser = new User(user, currentSocket, ipAddress, currClientSender); 
				dispatcher.addOnlineUser(currUser);
			}
			
			/*Actual start of the Thread*/
			try {
				while (isThreadOn) {
					String message = currSocketRead.readLine();
					dispatcher.addMessageToQ(currUser.getName(), message);
					
					/*Close connection by client`s request*/
					if (Constants.CLOSE_CONNECTION.equalsIgnoreCase(message)) {
						isThreadOn = false;
						hasClientClose = true;
						break;
					}
				}
			} catch (IOException iOEx) {
				System.out.println("Connection lost.");
				
				/*If connection breakdown - delete the user from list*/
				dispatcher.deleteOnlineUser(currUser.getName());
				System.out.println("User \"" + currUser.getName() +"\" disconnected");
				iOEx.printStackTrace();	
			} finally {
				try {
					if (!hasClientClose) {
						currSocketRead.close();
						currSocketWrite.close();
						currentSocket.close();
					}
				} catch (IOException iOEx) {
					System.out.println("Current Socket close failed!");
					iOEx.printStackTrace();
				}
			}
		}
		
	}
}
