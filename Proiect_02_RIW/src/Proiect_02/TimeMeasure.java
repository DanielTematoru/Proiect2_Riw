package Proiect_02;
import java.io.IOException;

public class TimeMeasure {
	
	public long executionTime()
	{
		long startTime = System.currentTimeMillis();

	      long total = 0;
	      for (int i = 0; i < 10000000; i++) {
	         total += i;
	      }

	      long stopTime = System.currentTimeMillis();
	      long elapsedTime = stopTime - startTime;
	      return elapsedTime;
	}

}
