package test;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import main.MarketData;
import main.MarketDataProcessor;

public class MarketDataTest {

	@Test
	public void testSingleOnMessage() throws InterruptedException
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
		
		while(true)
		{
			executorService.submit(marketDataProcessor);

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
			//Thread.sleep(1000);
		}
		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		while(true)
		{
			executorService.submit(marketDataProcessor);
		}		
	}
	
	@Test
	public void testMultipleOnMessageWithDifferentSymbol() throws InterruptedException
	{
		
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();
		
		int symbolCode = 100;
		
		for (int i=0; i<250; i++)
		{
			MarketData testData = new MarketData();
			int currentSymbol = symbolCode + i;
			testData.setPrice(i + 100.0);
			testData.setSymbol("0" + currentSymbol);
			testData.setUpdateTime(LocalDateTime.now());			
			marketDataProcessor.onMessage(testData);
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		while(true)
		{
			executorService.submit(marketDataProcessor);			
		}
	}
}
