package lab1;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
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
							len-=bufferSize;
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
							len-=bufferSize;
						}
						inFile.close();
						message.setFileName(fileName);
						
					}
					in.close();
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
			private void processMessage(Message m){
				
			}
		
	}
	
	class SendMessageProcessor implements Runnable{
		private volatile boolean isStop;
		private Message currentMessage;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!isStop){
				synchronized(sendingMessageQueue){
					while(sendingMessageQueue.isEmpty()){
						try {
							sendingMessageQueue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					currentMessage=sendingMessageQueue.poll();
					
				}
				
				try {
					currentMessage.setSocket(new Socket());
					currentMessage.getSocket().connect(new InetSocketAddress(currentMessage.getDestinationHostName(),currentMessage.getDestinationPort()));
					OutputStream out=currentMessage.getSocket().getOutputStream();
					out.write((byte)currentMessage.getRequestType());
					
					long contentLen=0;
					String content=currentMessage.getContent();
					byte contentBuffer[]=content.getBytes();
					if(content!=null&&!content.equals("")){
						contentLen=(long)contentBuffer.length;
						currentMessage.setContentLen(contentLen);
					}
					out.write(longToByte(contentLen));
					
					String fileName=currentMessage.getFileName();
					RandomAccessFile inFile=null;
					long fileLen=0;
					if(fileName!=null&&!fileName.equals("")){
						inFile=new RandomAccessFile(currentMessage.getFileName(),"r");
						fileLen=inFile.length();
						currentMessage.setFileLen(fileLen);
					}
					out.write(longToByte(fileLen));
					if(contentLen>0){
						out.write(currentMessage.getContent().getBytes());
					}
					if(fileLen>0){
						long len=fileLen;
						int bufferSize;
						while(len>0){
							if(len>1024)
								bufferSize=1024;
							else 
								bufferSize=(int) len;
							byte buffer[]=new byte[1024];
							inFile.read(buffer);
							out.write(buffer);
							len-=bufferSize;
						}
						inFile.close();
					}
					out.flush();
					out.close();
					currentMessage.getSocket().close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		private byte[] longToByte(long n){
			byte[] b = new byte[8];   
		    for (int i = 7; i >= 0; i--) {   
		      b[i] = (byte) (n % 256);   
		      n >>= 8;   
		    }   
		    return b;   
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
		Thread t2=new Thread(sendingProcessor);
		t2.start();
		
	}
	public void processReceivedNewMessage(Message message){
		synchronized(recevieMessageQueue){
			recevieMessageQueue.add(message);
			recevieMessageQueue.notify();
		}
	}
	public void sendMessage(Message message){
		synchronized(sendingMessageQueue){
			sendingMessageQueue.add(message);
			sendingMessageQueue.notify();
		}
	}


}
