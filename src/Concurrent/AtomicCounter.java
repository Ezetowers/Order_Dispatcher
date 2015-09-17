package concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import logger.Logger;
import logger.LogLevel;

public class AtomicCounter {
    public AtomicCounter() {
        lock_ = new ReentrantLock();
        counter_ = 0;
    }

    public void inc() {
        lock_.lock();
        ++counter_;
        Logger.log(LogLevel.TRACE, "[ATOMIC COUNTER] Counter value: " 
            + counter_);
        lock_.unlock();
    }

    public long counter() {
        lock_.lock();
        long aux = counter_;
        lock_.unlock();
        return aux;
    }

    private Lock lock_;
    private long counter_;
}