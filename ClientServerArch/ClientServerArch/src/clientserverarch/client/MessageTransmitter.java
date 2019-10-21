package clientserverarch.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import clientserverarch.constants.Constants;

/**
 * Transmits messages from the console to the server and vice versa.
 * @author bogi
 *
 */
public class MessageTransmitter implements Runnable {
	private PrintWriter currWriter;
	private BufferedReader currReader;
	private Client currClient;
	private boolean isConsToSocket;
	private boolean isOn;
	String read;
	
	public MessageTransmitter(Client currClient, BufferedReader currRead, PrintWriter currWrite, boolean isConsToSocket) {
		this.currClient = currClient;
		this.currReader = currRead;
		this.currWriter = currWrite;
		this.isConsToSocket = isConsToSocket;
		isOn = true;
	}
	
	public boolean checkMessage() {
		boolean check = (!(Constants.CLOSE_CONNECTION.equalsIgnoreCase(read))
				&& !(Constants.SERVER_SHUTDOWN.equalsIgnoreCase(read))
				&& !(read.contains(Constants.CLOSE_CONNECTION_BY_ADMIN))
				&& !(Constants.GET_USERS_LIST.equalsIgnoreCase(read))
				&& !(read.contains(Constants.CHANGE_USER_PASS))
				&& !(read.contains(Constants.DELETE_FILE_FROM_DIR))
				&& !(read.matches(Constants.MESSAGE_PATTERN))
				&& !(read.contains(Constants.FILE_DOWNLOAD))
				&& !(read.contains(Constants.FILE_UPLOAD))
				&& !(Constants.LIST_FILES_DIR.equalsIgnoreCase(read)));
		return check;
	}
	
	@Override
	public void run() {
		try {
			while (isOn) {	
				 
				read = currReader.readLine();
				if (read == null) {
					currReader.close();
					currWriter.close();
					if (currClient.getSocket() != null) {
						currClient.getSocket().close();
					}
					System.exit(0);
					break;
				}
				if (isConsToSocket) {
					
					/*
					 * XXX 
					 * Verifies message input format [receiver]|[message] by regex 
					 * and requires new input if format is not matching.
					 * System word is accepted. 
					 * 
					 * Check class Constants for more info on the regex.
					 */
					while (checkMessage()) {
						System.out.println("Incorrect message format [receiver]|[message]!");
						read = currReader.readLine();
					}
				}
				currWriter.println(read);
				currWriter.flush();
				
				/*Close connection by client`s request*/
				if (isConsToSocket && (Constants.CLOSE_CONNECTION.equalsIgnoreCase(read))) {
					break;
				}
				if (!isConsToSocket && ((Constants.CLOSE_CONNECTION.equalsIgnoreCase(read))
							|| Constants.CLOSE_CONNECTION_ADMIN_MESSAGE.equalsIgnoreCase(read)
							|| Constants.SERVER_SHUTDOWN.equalsIgnoreCase(read))) {
					break;
				}
			}
		} catch (IOException iOEx) {
				System.out.println("Connection lost!");
				iOEx.printStackTrace();
		} finally {
			try {
				
				/*Do not close the streams when Cons-to-Sock
				 *but only stop the thread, as the streams will be used.
				 *
				 *Close the streams and the socket when Sock-to-Cons
				 */
				if (isConsToSocket) {
					isOn = false;
				} else {
					if (Constants.SERVER_SHUTDOWN.equalsIgnoreCase(read)
							|| Constants.CLOSE_CONNECTION_ADMIN_MESSAGE.equalsIgnoreCase(read)) {
						System.exit(0);
					} else {
						isOn = false;
						currReader.close();
						currWriter.close();
						currClient.getSocket().close();
					}
					
				}
			} catch (IOException iOEx) {
				iOEx.printStackTrace();
			}
		}
	}
}

