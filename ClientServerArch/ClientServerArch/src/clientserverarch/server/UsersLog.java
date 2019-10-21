package clientserverarch.server;

import java.util.HashMap;
import java.util.Map;

import clientserverarch.constants.Constants;

public class UsersLog {
	private Map <String,User> allUsers;

	public UsersLog() {
		allUsers = new HashMap<String, User>();
		User admin = new User(Constants.ADMIN_NAME, Constants.ADMIN_PASS);
		allUsers.put(admin.getName(), admin);
	}
	
	/**
	 * This method checks user credentials.
	 * First checks if the user is already online, then checks if it exist in the all users list:
	 * if yes check the credentials (user name and password), 
	 * if no then create new user with given user name and password.
	 * 
	 * @param username        the user name of the client
	 * @param password        the password to the client
	 * @param dispatcher      the dispatcher holding list with online users
	 * @return                0 or 1 when login is successful
	 *                       -1 or 2 when login is not successful
	 */
	public synchronized int logIn(String username, String password, Dispatcher dispatcher) {
		int log;
		User user;
		if (dispatcher.isOnline(username)) {
			log = -1;
		} else {
			user = allUsers.get(username.toLowerCase());
			if (user == null) {
				addNewUser(username, password);
				log = 1;
			} else {
				if (user.isPassCorrect(password)) {
					log = 0;
				} else {
					log = 2;
				}
			}
		}
		return log;
	}

	public synchronized void addNewUser(String name, String pass) {
		User newUser = new User(name.toLowerCase(), pass);
		allUsers.put(newUser.getName(), newUser);
	}
	
	public void changePassword(String name, String pass) {
		User user = allUsers.get(name);
		user.changePassword(pass);
	}
}
