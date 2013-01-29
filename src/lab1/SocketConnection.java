package lab1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnection implements Runnable{

	/**
	 * @param args
	 */
	static int SOCKET_PORT=9213;
	static String LOCAL_HOSTNAME;
	private ServerSocket serversocket;
	private MessageHandler messageHandler;
	public SocketConnection() throws IOException{
		try {
			InetAddress addr = InetAddress.getLocalHost();
			LOCAL_HOSTNAME=addr.getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		messageHandler=new MessageHandler();
		serversocket=new ServerSocket(SOCKET_PORT);
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Socket s=serversocket.accept();
				Message m=new Message();
				m.setSocket(s);
				messageHandler.processReceivedNewMessage(m);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

}
