package clientserverarch.constants;

public final class Constants {
	public static final int DEFAULT_PORT = 8888;
	public static final int PORT = 3333;
	public static final int USER_MIN_LENGHT = 5;
	public static final int USER_MAX_LENGHT = 8;
	public static final int CLIENT_REQUEST_TIMEOUT = 15*60*1000; // 15 min.
	public static final int ARRAY_MESSAGE_SIZE = 2;
	public static final int FILE_MAX_SIZE = 5000000; /*5Mb*/
	public static final String ADMIN_NAME = "admin";
	public static final String ADMIN_PASS = "Admin123";
	public static final String CHANGE_USER_PASS = "!changepass";
	public static final String CLOSE_CONNECTION = "!endconn";
	public static final String CLOSE_CONNECTION_BY_ADMIN = "!endconn=";
	public static final String CLOSE_CONNECTION_ADMIN_MESSAGE = "!endconn by admin";
	public static final String DELETE_FILE_FROM_DIR = "!deletefile";
	public static final String DOWNLOAD_DIR_ABS_PATH = "D:\\Downloads";
	public static final String FILE_DOWNLOAD = "!downloadfile";
	public static final String FILE_UPLOAD = "!uploadfile";
	public static final String ESCAPING_VERTICAL_BAR = "\\|";
	public static final String ESCAPING_EQUAL_SIGN = "\\=";
	public static final String FILE_DIR_ABS_PATH = "D:\\JAVA\\Eclipse\\Workplace\\ClientServerArch v13\\FileDirectory";
	public static final String GET_USERS_LIST = "!listusers";
	public static final String LIST_FILES_DIR = "!listfiles";
	public static final String MESSAGE_TO_ALL = "to-all";
	public static final String MESSAGE_ADMIN_ONLY = "NOT Authorized! Administrator only!";
	public static final String SERVER_SHUTDOWN = "!stopserver";
	public static final String VERTICAL_BAR = "|";
	public static final String UPLOAD_DIR_ABS_PATH = "D:\\Uploads";
	
	
	/*
	 * Regex pattern for password input validation
	 * (?=.*[0-9]) a digit must occur at least once
	 * (?=.*[a-z]) a lower case letter must occur at least once
	 * (?=.*[A-Z]) an upper case letter must occur at least once
	 * (?=\\S+$) no whitespace allowed in the entire string
	 * .{6,} at least 6 characters
	 */
	public static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";
	
	/*
	 * XXX 
	 * Regex pattern for message input validation
	 * ^[A-Za-z0-9\\-]{5,8}\\|.{1,} no blank spaces before the vertical bar [receiver]|, can have spaces after that
	 * ^.{1,}[A-Za-z0-9\\-]{5,8}.{1,}\\|.{1,} can have blank spaces on both sides of the vertical bar.
	 * Here we use first option.
	 */
	public static final String MESSAGE_PATTERN = "^[A-Za-z0-9\\-]{5,8}\\|.{1,}";
}
