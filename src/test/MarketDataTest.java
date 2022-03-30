package test;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import main.MarketData;
import main.MarketDataProcessor;

public class MarketDataTest {
	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	@Test
	public void testSingleOnMessage() throws InterruptedException
	{
		/*
		 * Single message test to ensure it can send message (symbol: 0100 price: 100.0)
		 * 1 message
		 */
		MarketData testData = new MarketData();
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();
		testData.setPrice(100.0);
		testData.setSymbol("0100");
		testData.setUpdateTime(LocalDateTime.now());
		marketDataProcessor.onMessage(testData);

		LocalDateTime start = LocalDateTime.now();
		LocalDateTime current;		
		do {
			executorService.submit(marketDataProcessor);
			current = LocalDateTime.now();
		} while(Duration.between(start, current).getSeconds() <= 3);
		
		assertTrue(marketDataProcessor.getSendMessageCount() == 1);
	}
	
	@Test
	public void testMultipleOnMessageWithSameSymbol() throws InterruptedException
	{
		/*
		 * Same symbol 0100 with price for 100.0, expected sends out 100.0 (1st frame), 100.0 (2nd frame) and 100.0 (last frame)
		 * 201 message
		 */
		MarketData testData = new MarketData();
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();		
		
		for (int i=0; i<201; i++)
		{
			testData.setPrice(100.0);
			testData.setSymbol("0100");
			testData.setUpdateTime(LocalDateTime.now());			
			marketDataProcessor.onMessage(testData);
		}		
		
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime current;		
		do {
			executorService.submit(marketDataProcessor);
			current = LocalDateTime.now();
		} while(Duration.between(start, current).getSeconds() <= 10);	
		
		assertTrue(marketDataProcessor.getSendMessageCount() == 3);
	}
	
	@Test
	public void testMultipleOnMessageWithSameSymbolWithDifferentPrice() throws InterruptedException
	{
		/*
		 * Same symbol 0100 with price from 100.0 to 300.0 (in order), expected sends out 199.0 (1st frame), 299.0 (2nd frame) and 300.0 (last frame)
		 * 201 message
		 */
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();		
		
		for (int i=0; i<201; i++)
		{
			MarketData testData = new MarketData();
			testData.setPrice(i + 100.0);
			testData.setSymbol("0100");
			testData.setUpdateTime(LocalDateTime.now());			
			marketDataProcessor.onMessage(testData);
		}
		
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime current;		
		do {
			executorService.submit(marketDataProcessor);
			current = LocalDateTime.now();
		} while(Duration.between(start, current).getSeconds() <= 10);
		
		assertTrue(marketDataProcessor.getSendMessageCount() == 3);
	}
	
	@Test
	public void testMultipleOnMessageWithDifferentSymbol() throws InterruptedException
	{
		/*
		 * Different symbol from 0100 to 0349 with price from 100.0 to 349.0 (in order)
		 * expected sends out 100 msg (1st frame), 100 msg (2nd frame) and 50 msg (last frame)
		 * 250 message
		 * message sent will not be in order
		 */
		
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

		LocalDateTime start = LocalDateTime.now();
		LocalDateTime current;		
		do {
			executorService.submit(marketDataProcessor);
			current = LocalDateTime.now();
		} while(Duration.between(start, current).getSeconds() <= 10);
		
		assertTrue(marketDataProcessor.getSendMessageCount() == 250);
	}
	
	@Test
	public void testMultipleOnMessageWithDifferentSymbolHighLoading() throws InterruptedException
	{
		/*
		 * Different symbol from 1000 to 1449 with price from 100.0 to 549.0 (in order)
		 * expected sends out 100 msg (1st, 2nd, 3rd, 4th frame) and 50 msg (last frame)
		 * 450 message
		 * message sent will not be in order
		 */
		
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();
		
		int symbolCode = 1000;
		
		for (int i=0; i<450; i++)
		{
			MarketData testData = new MarketData();
			int currentSymbol = symbolCode + i;
			testData.setPrice(i + 100.0);
			testData.setSymbol("" + currentSymbol);
			testData.setUpdateTime(LocalDateTime.now());			
			marketDataProcessor.onMessage(testData);
		}
		
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime current;		
		do {
			executorService.submit(marketDataProcessor);
			current = LocalDateTime.now();
		} while(Duration.between(start, current).getSeconds() <= 10);
		
		assertTrue(marketDataProcessor.getSendMessageCount() == 450);
	}
}
