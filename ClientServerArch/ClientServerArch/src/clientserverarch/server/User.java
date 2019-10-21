package clientserverarch.server;

import java.net.Socket;

import clientserverarch.constants.Constants;

public class User {
	private String name;
	private String password;
	private String ipAddress;
	private Socket socket;
	private ClientSender clientSender;
	private boolean isAdministrator;
	
	public User(String name, String password) {   /*to be used in allUsers*/
		this.name = name.toLowerCase();
		this.password = password;
	}
	
	public User(String name, Socket socket, String ipAddress, ClientSender clientSender) {     /*to be used in onlineUsers*/
		this(name, "N/A");
		this.socket = socket;
		this.ipAddress = ipAddress;
		this.clientSender = clientSender;
		if (Constants.ADMIN_NAME.equalsIgnoreCase(name)) {
			isAdministrator = true;
		}
	}
	
	public boolean isAdministrator() {
		return isAdministrator;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public Socket getSocket() {
		return socket;
	}
	public ClientSender getClientSender() {
		return clientSender;
	}
	
	public boolean isPassCorrect(String password) {
		boolean correct = false;;
		if (this.password.equals(password)) {
			correct = true;
		}
		return correct;
	}
	public void changePassword(String password) {
		this.password = password;
	}
}
