package lab1;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;

public class MessageHandler {
	class ReceiveMessageProcessor implements Runnable{
		private volatile boolean isStop=false;
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
						String fileName=new Date().getTime()+"_"+currentMessage.hashCode()+".ser";
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
					processMessage(currentMessage);
					
			        
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
			private long getLong(byte[] bytes){
				ByteBuffer bb=ByteBuffer.wrap(bytes);
				return bb.getLong();
			}
			private void processMessage(final Message m){
				final String[] args=m.getContent().split(" ");
				switch(m.getRequestType()){
				case Message.REQUESTTYPE_SLAVE_UPDATE:
					if(args.length==3)
						Main.masterslavecontrol.addSlave(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
					break;
				case Message.REQUESTTYPE_NOTIFY_MIGRATION:
					
					if(args.length==3){
						Thread migrateThread=new Thread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Integer num=Integer.valueOf(args[2]);
								for(int i=1;i<=num;i++){
									MigratableProcess process=Main.threadPool.getSuspendedProcess();
									String fileName=(new Date()).getTime()+"_"+m.hashCode()+".ser";
									try {
										
										FileOutputStream fileOut=new FileOutputStream(fileName);
										ObjectOutputStream out=new ObjectOutputStream(fileOut);
										out.writeObject(process);
										out.close();
										fileOut.close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										
										e.printStackTrace();
										return;
									}
									Message newMessage=new Message();
									newMessage.setRequestType(Message.REQUESTTYPE_MIGRATION);
									newMessage.setFileName(fileName);
									newMessage.setContent(SocketConnection.LOCAL_HOSTNAME);
									newMessage.setDestinationHostName(args[0]);
									newMessage.setDestinationPort(Integer.valueOf(args[1]));
									sendMessage(newMessage);
									System.out.println(process.getClass().getName()+" has been migrated to "+args[0]+":"+args[1]);
									
								}
							}
							
						});
						migrateThread.start();
					}
					break;
						
				case Message.REQUESTTYPE_MASTER_UPDATE:
					if(args.length==2){
						Main.masterslavecontrol.beSlave(args[0], Integer.valueOf(args[1]));
					}
					break;
				case Message.REQUESTTYPE_MIGRATION:
					if(args.length==1 && m.getFileName()!=null&&!m.getFileName().equals("")){
						MigratableProcess process=null;;
						try{
						FileInputStream fileIn = new FileInputStream(m.getFileName());
						ObjectInputStream in = new ObjectInputStream(fileIn);
						process = (MigratableProcess) in.readObject();
						in.close();
						fileIn.close();
						}catch(IOException e){
							e.printStackTrace();
							return;
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
						Main.threadPool.addTask(process);
						System.out.println("Receive "+process.getClass().getName()+" from "+args[0]);
					}
					break;
				case Message.REQUESTTYPE_SLAVE_CHANGE_PORT:
					break;
				
				}
				
			}
		
	}
	
	class SendMessageProcessor implements Runnable{
		private volatile boolean isStop=false;
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
			ByteBuffer bb = ByteBuffer.allocate(8);  
	        return bb.putLong(n).array();  
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
