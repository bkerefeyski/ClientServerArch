package clientserverarch.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import clientserverarch.constants.Constants;

public class FileDirectory {
	public String action(String message) {
		
		/*Check if file abs path exists and is directory*/
		File f = new File(Constants.FILE_DIR_ABS_PATH);
		if (f.exists() && f.isDirectory()) {
			
			/*List files in directory*/
			if (Constants.LIST_FILES_DIR.equalsIgnoreCase(message)) {
				return listFiles();
			
			/*Download file from directory*/
			} else if (message.contains(Constants.FILE_DOWNLOAD)) {
				String fileName = getFileName(message);
				String absPathReadFile = Constants.FILE_DIR_ABS_PATH + "\\\\" + fileName;
				String absPathWriteFile = Constants.DOWNLOAD_DIR_ABS_PATH + "\\\\" + fileName;
				String returnMessage = "Download successfully completed";
				
				/*Check if file and directories exist*/
				File fRead = new File(absPathReadFile);
				if (!fRead.exists()) {
					return "File: \"" + fileName + "\" does NOT exists!";
				}
				
				File fDownloadDir = new File(Constants.DOWNLOAD_DIR_ABS_PATH);
				if (!fDownloadDir.exists()
						&& !fDownloadDir.isDirectory()) {
					return "Download directory does NOT exists! (" + Constants.DOWNLOAD_DIR_ABS_PATH + ")";
				}
				
				/*Actual download of file*/
				processFile(absPathReadFile, absPathWriteFile);
				
				return returnMessage;
			
			/*Upload file to server directory*/
			} else if (message.contains(Constants.FILE_UPLOAD)) {
				String fileName = getFileName(message);
				String absPathWriteFile = Constants.FILE_DIR_ABS_PATH + "\\\\" + fileName;
				String absPathReadFile = Constants.UPLOAD_DIR_ABS_PATH + "\\\\" + fileName;
				
				/*Check if file and directories exist*/
				File fUploadDir = new File(Constants.UPLOAD_DIR_ABS_PATH);
				if (!fUploadDir.exists()
						&& !fUploadDir.isDirectory()) {
					return "Upload directory does NOT exists! (" + Constants.UPLOAD_DIR_ABS_PATH + ")";
				}
				
				File fRead = new File(absPathReadFile);
				if (!fRead.exists()) {
					return "File: \"" + fileName + "\" does NOT exists!";
				}
				
				File fWrite = new File(absPathWriteFile);
				if (fWrite.exists()) {
					return "File: \"" + fileName + "\" already exists! Use different name!";
				}
				
				if (fRead.length() > Constants.FILE_MAX_SIZE) {
					return "File exceeds size limit! Max " + Constants.FILE_MAX_SIZE + " bytes!";
				}
				
				/*Actual upload of file*/
				processFile(absPathReadFile, absPathWriteFile);
				
				return "Upload successfully completed";
			} else {
				return "Something went wrong!";
			}
		} else {
			return "File directory does NOT exists! (" + Constants.FILE_DIR_ABS_PATH + ")";
		}
	}
	
	public String listFiles() {
		File workFile = new File(Constants.FILE_DIR_ABS_PATH);
		File[] list = workFile.listFiles();
		
		if (list.length == 0) {
			String noFiles = "No Files in the directory";
			return noFiles;
		} else {
			StringBuilder fileStr = new StringBuilder();
			for (File f : list) {
				fileStr.append("File: " + f.getName());
				fileStr.append("\n");
			}
			return fileStr.toString();
		}
	}
	
	public String getFileName(String message) {
		String [] messageSplit = message.split(Constants.ESCAPING_EQUAL_SIGN);
		String fileName = messageSplit[1];
		return fileName;
	}
	
	public void processFile(String absPathReadFile, String absPathWriteFile) {
		try (
				BufferedReader read = new BufferedReader(new FileReader(new File(absPathReadFile)));
				PrintWriter write = new PrintWriter(new FileWriter(new File(absPathWriteFile)));
		) {
			String line;
			while ((line=read.readLine()) != null) {
				write.println(line);
			}
			
		} catch (FileNotFoundException fNotFoundEx) {
			fNotFoundEx.printStackTrace();
		} catch (IOException iOex) {
			iOex.printStackTrace();
		}
//		try {
//			FileReader readFile = new FileReader(new File(absPathReadFile));
//			BufferedReader read = new BufferedReader(readFile);
//			
//			FileWriter writeFile = new FileWriter(new File(absPathWriteFile));
//			PrintWriter write = new PrintWriter(writeFile);
//			
//			String line;
//			while ((line=read.readLine()) != null) {
//				write.println(line);
//			}
//			write.close();
//			read.close();
//			
//		} catch (FileNotFoundException notFoundEx) {
//			notFoundEx.printStackTrace();
//		} catch (IOException iOEx) {
//			iOEx.printStackTrace();
//		}
	}
}
