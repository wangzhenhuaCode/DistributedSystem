package lab1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.Date;
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
	private Integer workingNum=0;
	//private Double averageTime;

	class Worker implements Runnable {
		private volatile boolean isStop;
		private ProcessStatus work;
		private AtomicBoolean mirgationLock = new AtomicBoolean();
		private AtomicBoolean needMigration = new AtomicBoolean();
		private volatile boolean isCheckPoint;
		private AtomicBoolean isworking = new AtomicBoolean();

		@Override
		public void run() {
			// TODO Auto-generated method stub

			while (!isStop) {

				synchronized (mirgationLock) {

					mirgationLock.set(false);
				}

				synchronized (taskQueue) {
					while (taskQueue.isEmpty()) {
						try {
							taskQueue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					work = taskQueue.poll();
				}
				synchronized (isworking) {
					isworking.set(true);
				}
				synchronized (workingNum) {
					workingNum++;
				}
				work.setStatus(ProcessStatus.RUNNING);
				isCheckPoint=true;
				while (isCheckPoint) {
					isCheckPoint=false;
					try {
						
						work.getProcess().run();

					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					synchronized (mirgationLock) {
						mirgationLock.set(true);
						mirgationLock.notify();
					}
					synchronized (needMigration) {
						while (needMigration.get()) {
							try {
								needMigration.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
		
				work.setStatus(ProcessStatus.FINISHED);
				synchronized (workingNum) {
					workingNum--;
				}
				synchronized (isworking) {
					isworking.set(false);
				}

			}

		}

		public void setWork(ProcessStatus work) {
			this.work = work;
		}

		public boolean isWorking() {
			synchronized (isworking) {
				if (isworking.get())
					return true;
				else
					return false;
			}
		}

		public boolean isCheckPoint() {
			return isCheckPoint;
		}

		public void setCheckPoint(boolean isCheckPoint) {
			this.isCheckPoint = isCheckPoint;
		}

		public Worker() {
			isStop = false;
			mirgationLock.set(false);
			needMigration.set(false);
		}

		public void stop() {
			isStop = true;
		}

		public ProcessStatus getWork() {
			return work;
		}

	}

	public ThreadPool(int size) {
		this.size = size;
		workerList = new ArrayList<Worker>();
		taskQueue = new LinkedList<ProcessStatus>();
		for (int i = 0; i < size; i++) {
			Worker worker = new Worker();
			workerList.add(worker);
			Thread t = new Thread(worker);
			t.start();
		}
		Thread checkThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
				checkPoint();
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}

		});
		checkThread.start();
	}

	public void addTask(ProcessStatus mp) {
		synchronized (taskQueue) {
			taskQueue.add(mp);
			taskQueue.notify();
		}
	}

	public void closePool() {
		Worker w;
		for (int i = 0; i < size; i++) {
			w = workerList.get(i);
			w.getWork().getProcess().suspend();
			w.stop();
		}

	}

	public int getTotalProcess() {
		return workingNum+taskQueue.size();
		
		
	}

	public ProcessStatus getSuspendedProcess() {
		int n = (int) (Math.random() * size);
		Worker w = workerList.get(n);
		while (!w.isWorking()) {
			n = (int) (Math.random() * size);
			w = workerList.get(n);
		}
		ProcessStatus process = null;

		synchronized (w.needMigration) {
			w.needMigration.set(true);
			w.getWork().getProcess().suspend();
			synchronized (w.mirgationLock) {
				while (!w.mirgationLock.get()) {
					try {
						w.mirgationLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				process = w.getWork();
				w.mirgationLock.set(false);
			}
			w.needMigration.set(false);
			w.needMigration.notify();
		}
		return process;
	}

	public void checkPoint() {
		synchronized (taskQueue) {
			
			for (int i = 0; i < taskQueue.size(); i++) {
				ProcessStatus process = taskQueue.get(i);
				try {
					FileOutputStream fileOut = new FileOutputStream(
							process.getProcessId() + ".ser");
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
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
		for (int i = 0; i < workerList.size(); i++) {
			Worker w = workerList.get(i);
			ProcessStatus process = null;
			if (w.isWorking()) {
				synchronized (w.needMigration) {
					w.needMigration.set(true);
					w.getWork().getProcess().suspend();
					synchronized (w.mirgationLock) {
						while (!w.mirgationLock.get()) {
							try {
								w.mirgationLock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						process = w.getWork();
						w.mirgationLock.set(false);
						try {
							FileOutputStream fileOut = new FileOutputStream(
									process.getProcessId() .replace(".", "_")+ ".ser");
							ObjectOutputStream out = new ObjectOutputStream(
									fileOut);
							out.writeObject(process.getProcess());
							out.close();
							fileOut.close();
							
						
							
							FileInputStream fileIn = new FileInputStream(process.getProcessId() .replace(".", "_")+".ser");
							ObjectInputStream in = new ObjectInputStream(fileIn);
							process.setProcess((MigratableProcess) in.readObject());
							in.close();
							fileIn.close();
						
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					w.isCheckPoint=true;
					w.needMigration.set(false);
					w.needMigration.notify();
				}
			}
		}
		System.out.println("check point");
	}

}
