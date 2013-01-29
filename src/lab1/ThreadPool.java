package lab1;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;



public class ThreadPool {

	/**
	 * @param args
	 */
	private List<Worker> workerList;
	private LinkedList<MigratableProcess> taskQueue;
	private int size;
	private Integer workingNum;
	class Worker implements Runnable{
		private volatile boolean isStop;
		private MigratableProcess work;
		private AtomicBoolean mirgationLock=new AtomicBoolean();
		private AtomicBoolean needMigration=new AtomicBoolean();
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
				
					try{
						
						work.run();
						
					}catch(RuntimeException e){
						e.printStackTrace();
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
			
		}
		public Worker(){
			isStop=false;
			mirgationLock.set(false);
			needMigration.set(false);
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
		workingNum=0;
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

	public int getProcessNum(){
		return workingNum+taskQueue.size();
	}
	public MigratableProcess getSuspendedProcess(){
		int n=(int) (Math.random()*workingNum);
		MigratableProcess process=null;
		Worker w=workerList.get(n);
		
		synchronized(w.needMigration){
			w.needMigration.set(true);
			w.getWork().suspend();
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
	

}
