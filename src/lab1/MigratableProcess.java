package lab1;
import java.io.Serializable;


public interface MigratableProcess extends Runnable,Serializable {
	public void suspend();
	
}
