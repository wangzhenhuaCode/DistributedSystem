package lab1;

import java.io.File;
import java.net.Socket;

public class Message {
	
	static final int REQUESTTYPE_SLAVE_UPDATE=1;
	static final int REQUESTTYPE_NOTIFY_MIGRATION=2;
	static final int REQUESTTYPE_MASTER_UPDATE=3;
	static final int REQUESTTYPE_MIGRATION=4;
	static final int REQUESTTYPE_RECOVERY=5;
	
	static final String devide1="!&";
	static final String devide2="@&";
	static final String devide3="#&";
	static final String devide4="$&";
	
	private int requestType;
	//private long FileLen;
	private long contentLen;
	private String content;
	//private String fileName;
	private Socket socket;
	private String destinationHostName;
	private int destinationPort;
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
	
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	/*public String getFileName() {
	return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileLen() {
		return FileLen;
	}
	public void setFileLen(long fileLen) {
		FileLen = fileLen;
	}
	*/
	public long getContentLen() {
		return contentLen;
	}
	public void setContentLen(long contentLen) {
		this.contentLen = contentLen;
	}

	public int getDestinationPort() {
		return destinationPort;
	}
	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}
	public String getDestinationHostName() {
		return destinationHostName;
	}
	public void setDestinationHostName(String destinationHostName) {
		this.destinationHostName = destinationHostName;
	}

	
	
}
