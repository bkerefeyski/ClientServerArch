package clientserverarch.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Queue;

import clientserverarch.constants.Constants;

public class ClientSender implements Runnable {
	private Queue<String> messageQ = null;
	private boolean isStart;
	private User currUser;                 /*Identifies the thread with a user*/
	private Dispatcher dispatcher;
	private Socket currentSocket;
	private PrintWriter currSocketWrite;
	
	public ClientSender(Socket currentSocket, Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.currentSocket = currentSocket;
		messageQ = new ArrayDeque<String>();
		try {
			this.currentSocket.setSoTimeout(Constants.CLIENT_REQUEST_TIMEOUT);
		} catch (SocketException socketEx) {
			socketEx.printStackTrace();
		}
		try {
			currSocketWrite = new PrintWriter(currentSocket.getOutputStream());
		} catch (IOException iOEx) {
			iOEx.printStackTrace();
		}
		isStart = true;
	}
	
	public synchronized void addMessageToSenderQ(String message) {
		messageQ.add(message);
		notify();
	}
	
	public synchronized String getNextMessage() {
		while (messageQ.size() == 0) {
			try {
				wait();
			} catch (InterruptedException interrupetedEx) {
				interrupetedEx.printStackTrace();
			}
		}
		String message = messageQ.poll();
		return message;
	}
	
	private void sendMessageToClient(String message) {
		currSocketWrite.println(message);
		currSocketWrite.flush();
	}
	
	@Override
	public void run() {
		try {
			while (isStart) {
				String fullMessage = getNextMessage();
				String[] messageArr = fullMessage.split(Constants.ESCAPING_VERTICAL_BAR);
				String user = messageArr[0];
				String message = messageArr[1];
				sendMessageToClient(message); /*this method flushes the message to client console*/
				
				/*Identifies the thread with a user*/
				currUser = dispatcher.getUser(user);
				/*
				 * Ends client connection by Constants.CLOSE_CONNECTION
				 * Use (message.indexOf(':') == -1) as a check
				 * otherwise if user submits [receiver]|[!endconn] thread will stop
				 * But thread should only stop if user submits [!endconn]
				 */
				if (((Constants.CLOSE_CONNECTION.equalsIgnoreCase(message)
						|| Constants.CLOSE_CONNECTION_ADMIN_MESSAGE.equalsIgnoreCase(message)
						|| Constants.SERVER_SHUTDOWN.equalsIgnoreCase(message)))
						&& (message.indexOf(':') == -1)) {
					isStart = false;
					break;
				}
			}
		} finally {
			try {
				if (currSocketWrite != null) {
					currSocketWrite.close();
				}
				if (currentSocket != null) {
					currentSocket.close();
				}
				/*Delete user from online users list*/
				if (dispatcher.isOnline(currUser.getName())) {
					dispatcher.deleteOnlineUser(currUser.getName());
				}
			} catch (IOException iOEx) {
				System.out.println("Current Socket close failed!");
				iOEx.printStackTrace();
			}
		}
	}
}
