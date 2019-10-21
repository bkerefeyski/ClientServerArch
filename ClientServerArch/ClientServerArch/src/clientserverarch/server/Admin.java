package clientserverarch.server;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import clientserverarch.constants.Constants;

public class Admin {
	private String sender;
	private String message;
	private HashMap<String,User> onlineUsers;
	private UsersLog allUsersList;
	
	public Admin(String sender, String message, HashMap<String,User> onlineUsers, UsersLog allUsersList) {
		this.sender = sender;
		this.message = message;
		this.onlineUsers = onlineUsers;
		this.allUsersList = allUsersList;
	}
	
	public String[] action() {
		String[] returnVal = new String[Constants.ARRAY_MESSAGE_SIZE];

		/*Get list with all online users*/
		if (Constants.GET_USERS_LIST.equalsIgnoreCase(message)) {
			returnVal[0] = sender;
			returnVal[1] = getOnlineUsers(onlineUsers);

		/*Change user password*/
		} else if (message.contains(Constants.CHANGE_USER_PASS)){
			returnVal = changePassword(sender, message, onlineUsers, allUsersList);

		/*Close connection of a certain user*/
		} else if (message.contains(Constants.CLOSE_CONNECTION_BY_ADMIN)) {
			returnVal = closeUserConnection(sender, message, onlineUsers);
			
		/*Stop the server*/
		} else if (Constants.SERVER_SHUTDOWN.equalsIgnoreCase(message)) {
			returnVal[0] = Constants.MESSAGE_TO_ALL;
			returnVal[1] = Constants.SERVER_SHUTDOWN;
		} else if (message.contains(Constants.DELETE_FILE_FROM_DIR)) {
			returnVal[0] = sender;
			returnVal[1] = deleteFile(message);
		}
		return returnVal;
	}
	
	private String deleteFile(String message) {
		String returnMessage = null;
		
		/*message pattern "!deletefile=[File.txt]"*/
		String[] messageSplit = message.split(Constants.ESCAPING_EQUAL_SIGN);
		String systemKey = messageSplit[0];
		String fileName = messageSplit[1];
		
		/*Check if system key is "!deletefile" as in class Dispatcher we compare by message.contains
		 * and the message may not be in format "!deletefile=[File.txt]"*/
		if (Constants.DELETE_FILE_FROM_DIR.equalsIgnoreCase(systemKey)) {
			String absPathToFile = Constants.FILE_DIR_ABS_PATH + "\\\\" + fileName;
			File workFile = new File(absPathToFile);
			if (workFile.exists()) {
				workFile.delete();
				returnMessage = "File: \"" + fileName + "\" DELETED!";
			} else {
				returnMessage = "File: \"" + fileName + "\" do NOT exist!";
			}
		} else {
			returnMessage = "Incorrect message format \"!deletefile=[File.txt]\"";
		}
		return returnMessage;
	}
	
	private String[] closeUserConnection(String sender, String message, HashMap<String,User> onlineUsers) {
		String[] returnVal = new String[Constants.ARRAY_MESSAGE_SIZE];
		
		/*message pattern "!endconn=[user]"*/
		String[] messageSplit = message.split(Constants.ESCAPING_EQUAL_SIGN);
		String systemKey = messageSplit[0];
		String user = messageSplit[1];
		
		/*Check if system key is !endconn as in class Dispatcher we compare by 
		 * message.contains and the message may not be the correct format. Also check the user is online*/
		if (Constants.CLOSE_CONNECTION.equalsIgnoreCase(systemKey)
				&& (onlineUsers.get(user) != null)) {
			returnVal[0] = user;
			returnVal[1] = Constants.CLOSE_CONNECTION_ADMIN_MESSAGE;
		} else {
			
			/*If the user is not online send message to the admin*/
			if (onlineUsers.get(user) == null) {
				returnVal = sendMessageToAdmin(sender,user);
			} else {
				returnVal[0] = sender;
				returnVal[1] = "Incorrect message format \"!endconn=[user]\"";
			}
		}
		return returnVal;
	}
	
	private String[] sendMessageToAdmin(String sender, String user) {
		String[] returnVal = new String[Constants.ARRAY_MESSAGE_SIZE];
		returnVal[0] = sender;
		returnVal[1] = "User \"" + user +"\" is not online.";
		
		return returnVal;
	}
	
	private String[] changePassword(String sender, String message, HashMap<String,User> onlineUsers, 
			UsersLog allUsersList) {
		String[] returnVal = new String[Constants.ARRAY_MESSAGE_SIZE];

		/*message pattern "!changepass=[user]=[pass]"*/
		String[] messageSplit = message.split(Constants.ESCAPING_EQUAL_SIGN);
		String systemKey = messageSplit[0];
		String user = messageSplit[1];
		String pass = messageSplit[2];

		/*checks that the message is in the required pattern and also that the new password matches the password pattern*/
		if ((Constants.CHANGE_USER_PASS.equalsIgnoreCase(systemKey)) 
				&& (onlineUsers.get(user) != null)           /*check if the user is online, change pass only to online users*/
				&& (pass.matches(Constants.PASSWORD_PATTERN))) {
			allUsersList.changePassword(user, pass);
			returnVal[0] = user;
			returnVal[1] = "Password changed by Administrator! New pass: " + pass;
		} else {
			
			/*If user is not online, inform admin*/
			if (onlineUsers.get(user) == null) {
				returnVal = sendMessageToAdmin(sender,user);
			} else if (!pass.matches(Constants.PASSWORD_PATTERN)) {
				returnVal[0] = sender;
				returnVal[1] = "Password do not match required pattern.";
			} else {
				returnVal[0] = sender;
				returnVal[1] = "Incorrect message format \"!changepass=[user]=[pass]\"";
			}
		}
		return returnVal;
	}
	
	private String getOnlineUsers(HashMap<String,User> onlineUsers) {
		String value = null;
		
		String usersList = onlineUsers.toString();
		String[] usersArr = usersList.split("(, )");
		String[] finalArr = new String[usersArr.length];
		
		for (int i = 0; i < usersArr.length; i++) {
			StringBuilder nameAndIp = new StringBuilder();
			
			if (i == 0) {
				String userName = usersArr[i].substring(1, usersArr[i].indexOf("="));
				String ip = onlineUsers.get(userName).getIpAddress();
				nameAndIp = buildUserNameAndIp(userName, ip);
			} else {
				String userName = usersArr[i].substring(0, usersArr[i].indexOf("="));
				String ip = onlineUsers.get(userName).getIpAddress();
				nameAndIp = buildUserNameAndIp(userName, ip);
			}
			finalArr[i] = nameAndIp.toString();
		}
		value = Arrays.toString(finalArr);
		return value;
	}
	
	private StringBuilder buildUserNameAndIp(String userName, String ip) {
		StringBuilder nameAndIp = new StringBuilder();
		nameAndIp.append(userName);
		nameAndIp.append(": ");
		nameAndIp.append(ip);
		
		return nameAndIp;
	}
}
