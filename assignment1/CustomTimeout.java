package HttpServer;
import java.util.Date;

public class CustomTimeout {
	
	private long startTime;
	private long elapsedTime = 0L;
	private long threshold;
	private boolean started;
	private boolean isStopped;

	public CustomTimeout(long thresholdSecond){
		this.threshold = thresholdSecond*60*1000;
		this.isStopped = false;
	}
	
	public void startTimer(){
		this.started = true;
		this.isStopped = false;
		this.startTime = System.currentTimeMillis();
	}
	
	public boolean isTimedout(){
		elapsedTime = (new Date()).getTime() - startTime;
		return (elapsedTime > this.threshold) && !isStopped;
	}
	
	public void resetTimer(){
		this.startTime = System.currentTimeMillis();
	}
	
	public boolean isTimerStarted(){
		return this.started;
	}
	
	public void stopTimer(){
		this.isStopped = true;
	}
	
}

