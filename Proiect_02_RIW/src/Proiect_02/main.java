package Proiect_02;
import java.io.IOException;


public class main {
	public static void main(String[] args) throws IOException{
		
		Crawler robotCrawler=new Crawler();
		long startTime = System.currentTimeMillis();
		
		robotCrawler.Q.add("http://riweb.tibeica.com/crawl/");
		robotCrawler.processesQueue();
		System.out.println("\nCele "+Restrictii.limitOfPages+ " link-uri au fost preluate cu succes!");
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
		System.out.println("Timpul de executie total este: "+elapsedTime/1000+" s");
	}
}


