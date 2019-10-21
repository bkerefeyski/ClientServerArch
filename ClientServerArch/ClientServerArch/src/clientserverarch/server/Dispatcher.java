/*
 *Class Dispatcher keeps two lists
 * Hash table with users that are currently online.
 * Queue with messages - adds to the Q (when received) and deletes from the Q (when message sent).
 */
package clientserverarch.server;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

//import clientserverarch.adminaction.AdminAction;
import clientserverarch.constants.Constants;

public class Dispatcher implements Runnable {
	private Queue<String[]> mQ = null;
	private HashMap<String,User> onlineUsers;
	private Server server;
	private UsersLog allUsersList;
	
	public Dispatcher(Server server, UsersLog allUsersList) {
		mQ = new ArrayDeque<String[]>();
		onlineUsers = new HashMap<String, User>();
		this.server = server;
		this.allUsersList = allUsersList;
	}
	
	public synchronized void addMessageToQ(String sender, String message) {
		String[] array = new String[2];
		
		/*Perform checks on the message content in order to handle it*/
		if (Constants.CLOSE_CONNECTION.equalsIgnoreCase(message)) {
			array[0] = sender;
			array[1] = message;
		} else if (Constants.LIST_FILES_DIR.equalsIgnoreCase(message) 
				|| message.contains(Constants.FILE_DOWNLOAD)
				|| message.contains(Constants.FILE_UPLOAD)) {
			array[0] = sender;
			FileDirectory fDir = new FileDirectory();
			array[1] = fDir.action(message);
		} else if (Constants.SERVER_SHUTDOWN.equalsIgnoreCase(message) 
				|| Constants.GET_USERS_LIST.equalsIgnoreCase(message)
				|| message.contains(Constants.CLOSE_CONNECTION_BY_ADMIN)
				|| message.contains(Constants.CHANGE_USER_PASS)
				|| message.contains(Constants.DELETE_FILE_FROM_DIR)) {
			
			if (Constants.ADMIN_NAME.equalsIgnoreCase(sender)) {
				
				/*Operations performed by Admin class*/
				Admin admin = new Admin(sender, message, onlineUsers, allUsersList);
				array = admin.action();
			} else {
				array[0] = sender;
				array[1] = Constants.MESSAGE_ADMIN_ONLY;
			}
		} else {
			String receiver = message.substring(0, message.indexOf(Constants.VERTICAL_BAR));
			receiver.trim();
			if (isOnline(receiver) || Constants.MESSAGE_TO_ALL.equalsIgnoreCase(receiver)) {
				array[0] = receiver;
				StringBuilder newMessage = new StringBuilder();
				String text = message.substring(message.indexOf(Constants.VERTICAL_BAR) + 1, message.length());
				newMessage.append(sender);
				newMessage.append(": ");
				newMessage.append(text);
				array[1] = newMessage.toString();
			} else {
				array[0] = sender;
				StringBuilder feedback = new StringBuilder();
				feedback.append("\"");
				feedback.append(receiver);
				feedback.append("\"");
				feedback.append(" is not online!");
				array[1] = feedback.toString();
			}
		}
		mQ.offer(array);
		notify();
	}
	
	public synchronized String[] getMessageFromQ() {
		while (mQ.size() == 0) {
			try {
				wait();
			} catch (InterruptedException interruptedEx) {
				interruptedEx.printStackTrace();
			}
		}
		String[] message = mQ.poll();
		return message;
	}
	
	private void sendMessage() {
		String[] messageArr = getMessageFromQ();
		String receiver = messageArr[0];
		String message = messageArr[1];
		String sender = null;
		
		if (message.indexOf(':') != -1) {
			sender = message.substring(0, message.indexOf(":"));
		}
		
		/*Send message to all online users*/
		if (Constants.MESSAGE_TO_ALL.equalsIgnoreCase(receiver)) {
			for (Map.Entry<String, User> current : onlineUsers.entrySet()) {
				User currentU = current.getValue();
				if (!currentU.getName().equalsIgnoreCase(sender)) {
					String finalMessage = createFinalMessage(currentU.getName(), message);
					currentU.getClientSender().addMessageToSenderQ(finalMessage);
				}
			}
			if (Constants.SERVER_SHUTDOWN.equalsIgnoreCase(message)) {
				server.stop();
			}
		} else {
			
			/*Send message to a certain user*/
			User user = getUser(receiver);
			String finalMessage = createFinalMessage(user.getName(), message);
			user.getClientSender().addMessageToSenderQ(finalMessage);
		}
	}
	/*Create message in the format [user]|[message], this is in order the ClientSender to be able to identify the user
	 * so that when connection is closed to remove the user from the list - onlineUsers*/
	private String createFinalMessage(String user, String message) {
		StringBuilder finalMessage = new StringBuilder();
		finalMessage.append(user);
		finalMessage.append(Constants.VERTICAL_BAR);
		finalMessage.append(message);
		return finalMessage.toString();
	}
	
	public synchronized void addOnlineUser(User user) {
		onlineUsers.put(user.getName(), user);
	}
	
	public synchronized void deleteOnlineUser(String name) {
		onlineUsers.remove(name);
	}
	
	public synchronized User getUser(String uName) {
		User user = onlineUsers.get(uName);
		return user;
	}
	
	public synchronized boolean isOnline(String name) {
		boolean online = false;
		User u = onlineUsers.get(name.toLowerCase());
		
		if (u != null) {
			online = true;
		}
		return online;
	}
	
	public synchronized HashMap<String,User> getOnlineUsers() {
		return onlineUsers;
	}
	
	@Override
	public void run() {
		while (server.getIsServerStarted()) {
			sendMessage();
		}
	}
}
