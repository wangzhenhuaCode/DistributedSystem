package lab1;

import java.util.Date;

public class ProcessStatus {
	static final int WAITING=1;
	static final int RUNNING=2;
	static final int FINISHED=3;
	static final int MIGRATED=0;
	private String processId;
	private MigratableProcess process;
	private String nameAndArgs;
	private Integer status;

	private Date statusChangeTime;
	private String runningMachine;
	



	public ProcessStatus(String processId, MigratableProcess process,
			String nameAndArgs, Integer status, String runningMachine,
			Date statusChangeTime) {
		super();
		this.processId = processId;
		this.process = process;
		this.nameAndArgs = nameAndArgs;
		this.status = status;
		this.runningMachine=runningMachine;
		this.statusChangeTime = statusChangeTime;
	}
	public ProcessStatus() {
		// TODO Auto-generated constructor stub
	}
	public MigratableProcess getProcess() {
		return process;
	}
	public void setProcess(MigratableProcess process) {
		this.process = process;
	}
	public String getNameAndArgs() {
		return nameAndArgs;
	}
	public void setNameAndArgs(String nameAndArgs) {
		this.nameAndArgs = nameAndArgs;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
		
	}


	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public Date getStatusChangeTime() {
		return statusChangeTime;
	}
	public void setStatusChangeTime(Date statusChangeTime) {
		this.statusChangeTime = statusChangeTime;
	}

	public void setRunningMachine(String runningMachine) {
		this.runningMachine = runningMachine;
	}
	public String getRunningMachine() {
		return runningMachine;
	}
	public String getStatutsInfo(){
		if(status==WAITING)return "Waiting";
		else if(status==RUNNING)return "Running";
		else if(status==MIGRATED)return "Migrated";
		else return "Finished";
	}
	
}
