package main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MarketDataProcessor implements Runnable{
	//Queue for storing all MarketData received upstream
	private ConcurrentLinkedQueue<MarketData> queue = new ConcurrentLinkedQueue<MarketData>();
	//Double-ended queue (use as Stack) for temporary store the MarketData for same window frame
	private ConcurrentLinkedDeque<MarketData> tempStack = new ConcurrentLinkedDeque<MarketData>();
	//Map for storing and checking same symbol
	private Map<String, LocalDateTime> symbolMap = new HashMap<String, LocalDateTime>();
	//Mark the start point of window frame
	private LocalDateTime windowFrameStart;
	//counter for message in same window frame
	private int counter = 0;
	//tdd test variable to check if number of message sent is expected
	private int sendMessageCount = 0;
	//MarketData Obj to store the latest retrieved MarketData obj
	private MarketData latestMarketData;
	//Lock for sychronizing multithreading
	private static final Object lock = new Object();
	
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
	
	@Override
	public void run() {
		synchronized(lock)
		{
			if(counter == 0)
			{
				windowFrameStart = LocalDateTime.now();
			}
			if(counter < 100 && !queue.isEmpty())
			{
				latestMarketData = queue.poll();
				tempStack.addFirst(latestMarketData);
				counter++;
			}
		
			else
			{		
				checkStackAndSend();
				latestMarketData = queue.peek();
				if(checkTimeFrame(windowFrameStart,LocalDateTime.now()))
				{
					tempStack.add(latestMarketData);
					counter = 1;
					windowFrameStart = LocalDateTime.now();
				}
				else
				{
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
	}
	
	//Check if the interval is longer than window frame
	public boolean checkTimeFrame(LocalDateTime from, LocalDateTime to)
	{
		long windowFrame = 1000; //millisecond
		long inteval = Math.abs(Duration.between(from, to).toMillis());
		/*
		 * true = outside window frame, can add new entry
		 * false = inside same window frame, trigger throttle control
		 */
		return (inteval > windowFrame)? true : false;
	}
	
	//Deduce how long to wait for next window frame
	public long checkIntevalDiff(LocalDateTime from, LocalDateTime to)
	{
		long windowFrame = 1000; //millisecond
		long inteval = Duration.between(from, to).toMillis();
		return Math.abs(inteval - windowFrame);
	}
	
	//To check if same symbol data is being received inside same window frame
	public boolean checkSymbolUpdate(MarketData data)
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
			if(checkTimeFrame(symbolMap.get(data.getSymbol()), LocalDateTime.now()))
				return true;
			else
				return false;
		}
	}	
	
	//Process the temporary stack
	public void checkStackAndSend()
	{
		while (!tempStack.isEmpty())
		{
			MarketData stackMarketData = tempStack.poll();
			if (checkSymbolUpdate(stackMarketData))
			{
				send(stackMarketData);
				setSendMessageCount(getSendMessageCount() + 1);
			}
		}
	}
	
	//send out the data, and store the latest updated time for symbol sent
	public void send(MarketData dataSend) {
		publishAggregatedMarketData(dataSend);
		
		//In console will print out the Symbol + Price + Sending time
		System.out.println("Sent data: " + dataSend.getSymbol() + " " + dataSend.getPrice() + " " + LocalDateTime.now());
		symbolMap.put(dataSend.getSymbol(), LocalDateTime.now());	
	}
	
	public int getSendMessageCount() {
		return sendMessageCount;
	}

	public void setSendMessageCount(int sendMessageCount) {
		this.sendMessageCount = sendMessageCount;
	}
	
}
