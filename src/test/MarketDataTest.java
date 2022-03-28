package test;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import main.MarketData;
import main.MarketDataProcessor;

public class MarketDataTest {

	@Test
	public void testSingleOnMessage()
	{
		MarketData testData = new MarketData();
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();
		testData.setPrice(100.0);
		testData.setSymbol("0100");
		testData.setUpdateTime(LocalDateTime.now());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		marketDataProcessor.onMessage(testData);
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {
			while(true)
			{
				executorService.submit(marketDataProcessor);
				Thread.sleep(10);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMultipleOnMessageWithSameSymbol() throws InterruptedException
	{
		MarketData testData = new MarketData();
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();		
		
		for (int i=0; i<101; i++)
		{
			testData.setPrice(100.0);
			testData.setSymbol("0100");
			testData.setUpdateTime(LocalDateTime.now());			
			marketDataProcessor.onMessage(testData);
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {
			while(true)
			{
				executorService.submit(marketDataProcessor);
				//Thread.sleep(1000);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
