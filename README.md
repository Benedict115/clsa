Assumption:
1. Message coming are in order. The format of all message received are correct (no malform message).
2. Application is being called by onMessage(MarketData) for receiving Market Data message. onMessage() method is expected to be message driven.
3. Assume single thread can handle the outcoming message task with normal performance
4. The ConcurrentLinkedQueue assumes to have enough memory to handle all the message. The application does not have logic on guarding the number of message saved in the queue. i.e. It may have out of memory issue if it received and accumulated too many message in queue and cannot send out due to throttle rate.
5. For point 4, assume message rate is guarded by Upstream application.
