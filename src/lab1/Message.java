package lab1;

import java.io.File;
import java.net.Socket;

public class Message {
	
	private int requestType;
	private long FileLen;
	private long contentLen;
	private String content;
	private String fileName;
	private Socket socket;
	public int getRequestType() {
		return requestType;
	}
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public long getFileLen() {
		return FileLen;
	}
	public void setFileLen(long fileLen) {
		FileLen = fileLen;
	}
	public long getContentLen() {
		return contentLen;
	}
	public void setContentLen(long contentLen) {
		this.contentLen = contentLen;
	}

	
	
}
