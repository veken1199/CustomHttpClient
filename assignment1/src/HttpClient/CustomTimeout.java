package HttpClient;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.management.RuntimeErrorException;

public class CustomTimeout implements Runnable {
	
	private long startTime;
	private long elapsedTime = 0L;
	private long threshold;
	private boolean isStarted;
	private boolean isStopped;

	public CustomTimeout(long thresholdSecond){
		this.threshold = thresholdSecond*60*10;
		this.isStopped = false;
		this.isStarted = false;
	}
	
	public void run()throws RuntimeException{
		this.startTimer();
	}
	
	public void startTimer(){
		this.isStarted = true;
		this.isStopped = false;
		this.startTime = System.currentTimeMillis();
		while(!this.isTimedout()){
			
		}throw new RuntimeException("error timeout");
	}
	
	public boolean isTimedout(){
		elapsedTime = (new Date()).getTime() - startTime;
		return (elapsedTime > this.threshold) && !isStopped;
	}
	
	public void resetTimer(){
		this.startTime = System.currentTimeMillis();
	}
	
	public boolean isTimerStarted(){
		return this.isStarted;
	}
	
	public boolean isTimerStopped(){
		return this.isStopped = true;
	}
	
	public void stopTimer(){
		this.elapsedTime = 0;
		this.isStopped = true;
	}

	
}

