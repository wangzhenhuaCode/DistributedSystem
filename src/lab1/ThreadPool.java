package lab1;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;



public class ThreadPool {

	/**
	 * @param args
	 */
	private List<Worker> workerList;
	private LinkedList<MigratableProcess> taskQueue;
	private int size;
	class Worker implements Runnable{
		private volatile boolean isStop;
		private MigratableProcess work;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			work=null;
			while(!isStop){
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
				
				try{
					work.run();
				}catch(RuntimeException e){
					e.printStackTrace();
				}
				work=null;
			}
			
		}
		public Worker(){
			isStop=false;
		}
		public void stop(){
			isStop=true;
		}
		public MigratableProcess getWork() {
			return work;
		}
		
	}
	
	public ThreadPool(int size){
		this.size=size;
		workerList=new ArrayList<Worker>();
		taskQueue=new LinkedList<MigratableProcess>();
		for(int i=0;i<size;i++){
			Worker worker=new Worker();
			workerList.add(worker);
			Thread t=new Thread(worker);
			t.start();
		}
		
	}
	
	public MigratableProcess removeTask(String className){
		synchronized(taskQueue){
			MigratableProcess mp;
			for(int i=0;i<size;i++){
				mp=workerList.get(i).getWork();
				if(mp.getClass().getName().equals(className)){
					mp.suspend();
					return mp;
				}
			}
			for(int i=0;i<taskQueue.size();i++){
				mp=taskQueue.get(0);
				if(mp.getClass().getName().equals(className)){
					return mp;
				}
			}
		}
		return null;
	}
	public void addTask(MigratableProcess mp){
		synchronized(taskQueue){
			taskQueue.add(mp);
			taskQueue.notify();
		}
	}
	public void closePool(){
		Worker w;
		for(int i=0;i<size;i++){
			w=workerList.get(i);
			w.getWork().suspend();
			w.stop();
		}
		
	}
	public void showStatus(){
		synchronized(taskQueue){
			MigratableProcess mp;
			for(int i=0;i<size;i++){
				mp=workerList.get(i).getWork();
				if(mp!=null)
					System.out.println("Process: "+mp.getClass().getName()+"  running.");
			}
			for(int i=0;i<taskQueue.size();i++){
				mp=taskQueue.get(0);
				System.out.println("Process: "+mp.getClass().getName()+"  waiting.");
			}
		}
	}

}
