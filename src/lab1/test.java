package lab1;

public class test implements MigratableProcess{

	@Override
	public void run() {
		
			for(int i=0; i<10; i++){
				try {
					Thread.currentThread();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.print(i);
			}
			
		
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}
	
}
