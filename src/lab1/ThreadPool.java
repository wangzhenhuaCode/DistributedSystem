package lab1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;



public class ThreadPool {

	/**
	 * @param args
	 */
	private List<Worker> workerList;
	private LinkedList<ProcessStatus> taskQueue;
	private int size;
	private Integer workingNum;
	class Worker implements Runnable{
		private volatile boolean isStop;
		private ProcessStatus work;
		private AtomicBoolean mirgationLock=new AtomicBoolean();
		private AtomicBoolean needMigration=new AtomicBoolean();
		private volatile boolean isCheckPoint;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			while(!isStop){
				
				synchronized(mirgationLock){
					
					mirgationLock.set(false);
				}
					
				synchronized(taskQueue){
					while(taskQueue.isEmpty()){
						try {
							taskQueue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					work=taskQueue.poll();
				}
				synchronized(workingNum){
					workingNum++;
				}
				
				synchronized(Main.processStatusList){
					for(ProcessStatus ps:Main.processStatusList){
						if(work==ps.getProcess()){
							ps.setStatus(ProcessStatus.RUNNING);
							break;
						}
					}
				}
				while(isCheckPoint){
					try{
						
						work.getProcess().run();
						
					}catch(RuntimeException e){
						e.printStackTrace();
					}
					synchronized(mirgationLock){
						mirgationLock.set(true);
						mirgationLock.notify();
					}
					synchronized(needMigration){
						while(needMigration.get()){
							try {
								needMigration.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}	
				}
				
				synchronized(Main.processStatusList){
						for(ProcessStatus ps:Main.processStatusList){
							if(work==ps.getProcess()){
								ps.setStatus(ProcessStatus.FINISHED);
								break;
							}
						}
				}
					synchronized(workingNum){
						workingNum--;
					}
					
				
			}
			
		}
		
		public boolean isCheckPoint() {
			return isCheckPoint;
		}

		public void setCheckPoint(boolean isCheckPoint) {
			this.isCheckPoint = isCheckPoint;
		}

		public Worker(){
			isStop=false;
			mirgationLock.set(false);
			needMigration.set(false);
		}
		public void stop(){
			isStop=true;
		}
		public ProcessStatus getWork() {
			return work;
		}
		
	}
	
	public ThreadPool(int size){
		this.size=size;
		workingNum=0;
		workerList=new ArrayList<Worker>();
		taskQueue=new LinkedList<ProcessStatus>();
		for(int i=0;i<size;i++){
			Worker worker=new Worker();
			workerList.add(worker);
			Thread t=new Thread(worker);
			t.start();
		}
		Thread checkThread=new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				checkPoint();
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		checkThread.start();
	}
	
	public void addTask(ProcessStatus mp){
		synchronized(taskQueue){
			taskQueue.add(mp);
			taskQueue.notify();
		}
	}
	public void closePool(){
		Worker w;
		for(int i=0;i<size;i++){
			w=workerList.get(i);
			w.getWork().getProcess().suspend();
			w.stop();
		}
		
	}

	public int getProcessNum(){
		return workingNum+taskQueue.size();
	}
	public ProcessStatus getSuspendedProcess(){
		int n=(int) (Math.random()*workingNum);
		ProcessStatus process=null;
		Worker w=workerList.get(n);
		
		synchronized(w.needMigration){
			w.needMigration.set(true);
			w.getWork().getProcess().suspend();
			synchronized(w.mirgationLock){
				while(!w.mirgationLock.get()){
					try {
						w.mirgationLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				process=w.getWork();
				w.mirgationLock.set(false);
			}
			w.needMigration.set(false);
			w.needMigration.notify();
		}
		return process;
	}
	public void checkPoint(){
		synchronized(taskQueue){
			for(int i=0;i<workerList.size();i++){
				Worker w=workerList.get(i);
				ProcessStatus process=null;
				synchronized(w.needMigration){
					w.needMigration.set(true);
					w.getWork().getProcess().suspend();
					synchronized(w.mirgationLock){
						while(!w.mirgationLock.get()){
							try {
								w.mirgationLock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						process=w.getWork();
						w.mirgationLock.set(false);
						try {
							FileOutputStream fileOut = new FileOutputStream(process.getProcessId());
							ObjectOutputStream out=new ObjectOutputStream(fileOut);
							out.writeObject(process.getProcess());
							out.close();
							fileOut.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					w.needMigration.set(false);
					w.needMigration.notify();
				}
			}
			for(int i=0;i<taskQueue.size();i++){
				ProcessStatus process=taskQueue.get(i);
				try {
					FileOutputStream fileOut = new FileOutputStream(process.getProcessId());
					ObjectOutputStream out=new ObjectOutputStream(fileOut);
					out.writeObject(process.getProcess());
					out.close();
					fileOut.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
 
}
