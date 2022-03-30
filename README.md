Assumption:
1. Message coming are in order. The format of all message received are correct (no malform message).
2. Application is being called by onMessage(MarketData) for receiving Market Data message. onMessage() method is expected to be message driven.
3. Assume message sent are not in order, because MarketDataProcessor need to cache the message to ensure sending the latest market data with same symbol.
4. The ConcurrentLinkedQueue assumes to have enough memory to handle all the message. The application does not have logic on guarding the number of message saved in the queue. i.e. It may have out of memory issue if it received and accumulated too many message in queue and cannot send out due to throttle rate.
5. For point 4, assume message rate is guarded by Upstream application, or the environment setup for MarketDataProcessor is sufficient to handle with the known message rate.

Method to run the MarketDataProcessor:
Firstly please compile for the code in src with command "javac MarketDataProcessor.java". Then please run by command "java MarketDataProcessor". It will start the non-stop threadpool for handling the incoming message. After that simply call the onMessage(MarketData) method to send the market data to MarketDataProcessor.

Method to run the test case MarketDataTest:
Suggest to run the test case one by one. This is because I have encountered a problem that the threadpool did not work if I run all the JUnit test at the same time.
