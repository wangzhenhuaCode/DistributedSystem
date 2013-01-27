package lab1;


import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;

public class MessageHandler {
	class ReceiveMessageProcessor implements Runnable{
		private volatile boolean isStop;
		private Message currentMessage;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!isStop){
				synchronized(recevieMessageQueue){
					while(recevieMessageQueue.isEmpty()){
						try {
							recevieMessageQueue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					currentMessage=recevieMessageQueue.poll();
				}
				try {
					InputStream in=currentMessage.getSocket().getInputStream();
					
					int requestType=in.read();
					if(requestType==-1){
						currentMessage.getSocket().close();
						continue;
					}
					Message message=new Message();
					message.setRequestType(requestType);
					
					
				
					byte longBuffer[]=new byte[8];
					in.read(longBuffer);
					long contentlen=getLong(longBuffer);
					message.setContentLen(contentlen);
					
					byte longBuffer2[]=new byte[8];
					in.read(longBuffer2);
					long filelen=getLong(longBuffer2);
					message.setFileLen(filelen);
					
					int bufferSize;
					
					if(message.getContentLen()>0){
						long len=message.getContentLen();
						String content="";
						while(len>0){
							if(len>1024)
								bufferSize=1024;
							else 
								bufferSize=(int) len;
							byte contentBuffer[]=new byte[bufferSize];
							in.read(contentBuffer);
							content=content+new String(contentBuffer);
							len-=1024;
						}
						message.setContent(content);
					}
					
					if(message.getFileLen()>0){
						long len=message.getFileLen();
						String fileName=new Date().getTime()+"_"+currentMessage.hashCode()+".os";
						RandomAccessFile inFile=new RandomAccessFile(fileName,"rw");
					
						while(len>0){
							if(len>1024)
								bufferSize=1024;
							else 
								bufferSize=(int) len;
							byte contentBuffer[]=new byte[bufferSize];
							in.read(contentBuffer);
							inFile.write(contentBuffer);
							len-=1024;
						}
						inFile.close();
						message.setFileName(fileName);
						
					}
					
					currentMessage.getSocket().close();
					
					
			        
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
			private long getLong(byte[] bytes){
				
				return(0xffL & (long)bytes[0]) | (0xff00L & ((long)bytes[1] << 8)) | (0xff0000L & ((long)bytes[2] << 16)) | (0xff000000L & ((long)bytes[3] << 24)) | (0xff00000000L & ((long)bytes[4] << 32)) | (0xff0000000000L & ((long)bytes[5] << 40)) | (0xff000000000000L & ((long)bytes[6] << 48)) | (0xff00000000000000L & ((long)bytes[7] << 56));
			}
		
	}
	
	class SendMessageProcessor implements Runnable{
		private volatile boolean isStop;
		private Message currentMessage;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private LinkedList<Message> recevieMessageQueue;
	private LinkedList<Message> sendingMessageQueue;
	private ReceiveMessageProcessor receiveProcessor;
	private SendMessageProcessor sendingProcessor;
	
	public MessageHandler(){
		recevieMessageQueue=new LinkedList<Message>();
		sendingMessageQueue=new LinkedList<Message>();
		receiveProcessor=new ReceiveMessageProcessor();
		Thread t=new Thread(receiveProcessor);
		t.start();
		sendingProcessor=new SendMessageProcessor();
		
		
	}
	public void processReceivedNewMessage(Message message){
		synchronized(recevieMessageQueue){
			recevieMessageQueue.add(message);
			recevieMessageQueue.notify();
		}
	}
	public


}
