package main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MarketDataProcessor implements Runnable{
	private ConcurrentLinkedQueue<MarketData> queue = new ConcurrentLinkedQueue<MarketData>();;
	private Map<String, LocalDateTime> symbolMap = new HashMap<String, LocalDateTime>();
	private LocalDateTime windowFrameStart;
	private MarketData latestMarketData;
	private int counter = 0;	
	
	// Receive incoming market data
	public void onMessage(MarketData data) {
		// Please implement
		//Assume message driven onMessage method thus no thread for adding the new market data
		queue.add(data);
		System.out.println("onMessage Called " + queue.size());
	}

	// Publish aggregated and throttled market data
	public void publishAggregatedMarketData(MarketData data) {
	// Do Nothing, assume implemented.
	}
	
	
	public boolean checkTimeFrame(LocalDateTime from, LocalDateTime to)
	{
		long windowFrame = 1000; //millisecond
		long inteval = Math.abs(Duration.between(from, to).toMillis());
		System.out.println(inteval + "ms");
		/*
		 * true = outside window frame, can add new entry
		 * false = inside same window frame, trigger throttle control
		 */
		return (inteval > windowFrame)? true : false;
	}
	
	public long checkIntevalDiff(LocalDateTime from, LocalDateTime to)
	{
		long windowFrame = 1000; //millisecond
		long inteval = Duration.between(from, to).toMillis();
		//System.out.println(inteval + "ms");
		return Math.abs(inteval - windowFrame);
	}
	
	public long checkSymbolUpdateDiff(LocalDateTime from, LocalDateTime to)
	{
		long inteval = Duration.between(from, to).toMillis();
		//System.out.println(inteval + "ms");
		return Math.abs(inteval);
	}
	
	public boolean checkSymbolupdate(MarketData data)
	{
		/*
		 * true = symbol update outside window frame, symbol can update.
		 * false = inside same window frame, symbol should not update
		 */
		if(symbolMap.get(data.getSymbol()) == null)
		{
			return true;
		}
		else
		{
			if(checkSymbolUpdateDiff(symbolMap.get(data.getSymbol()), LocalDateTime.now()) > 1000)
				return true;
			else
				return false;
		}
	}

	@Override
	public void run() {
		if(counter < 100 && !queue.isEmpty())
		{
			System.out.println("Scene 1");
			if(counter == 0)
			{
				windowFrameStart = LocalDateTime.now();
				//System.out.println("initialize");
			}
			latestMarketData = queue.peek();
			if(checkSymbolupdate(latestMarketData))
			{
				pollAndSend();
				
			}
			else
			{
				queue.poll();
			}
			counter++;
			//else
			//{
				//queue.poll();
			//}
		}
		else
		{
			latestMarketData = queue.peek();
			if(checkTimeFrame(windowFrameStart,LocalDateTime.now()))
			{
				System.out.println("Scene 2");
				pollAndSend();
				counter = 1;
				windowFrameStart = LocalDateTime.now();
			}
			else
			{
				System.out.println("Scene 3");
				try
				{
					Thread.sleep(checkIntevalDiff(windowFrameStart,LocalDateTime.now()));
					counter = 0;
					windowFrameStart = LocalDateTime.now();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}

	public void pollAndSend() {
		publishAggregatedMarketData(latestMarketData);
		symbolMap.put(latestMarketData.getSymbol(), LocalDateTime.now());
		queue.poll();
		System.out.println("Sent data: " + latestMarketData.getSymbol() + " " + latestMarketData.getPrice() + " " + LocalDateTime.now());
	}
	
	public static void main (String args[])
	{
		MarketDataProcessor marketDataProcessor = new MarketDataProcessor();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		System.out.println("main called");
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

}
