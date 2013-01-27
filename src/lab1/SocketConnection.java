package lab1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketConnection implements Runnable{

	/**
	 * @param args
	 */
	static int SOCKET_PORT=9213;
	private ServerSocket serversocket;
	private MessageHandler messageHandler;
	public SocketConnection(){
		messageHandler=new MessageHandler();
		try {
			serversocket=new ServerSocket(SOCKET_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
