package concurrent;

import java.lang.Thread;
import java.util.concurrent.BlockingQueue;
import logger.Logger;
import logger.LogLevel;


public abstract class Worker<TASK> extends Thread {
    public Worker(long threadId, 
                  String logPrefix, 
                  BlockingQueue<TASK> queue) {
        queue_ = queue;
        threadId_ = threadId;
        // logPrefix_ = "[" + logPrefix + " Thread ID: " + threadId_ + "] ";
        stop_ = false;
        logPrefix_ = "[TID: " + threadId_ + "] ";
    }

    public void run() {
        Logger.log(LogLevel.INFO, logPrefix_ + "Proceed to start Thread");
        while (! Thread.interrupted() && ! stop_) {
            try {
                this.execute();
            }
            catch (InterruptedException e) {
                // In case the interrupt variable were not set
                this.interrupt();
                break;
            }
        }

        if (stop_) {
            Logger.log(LogLevel.NOTICE, logPrefix_ 
                + "Thread stopped because max amount of URLs to analyzer "
                + "was reached");
        }
        Logger.log(LogLevel.INFO, logPrefix_ + "Thread ended.");
    }

    public abstract void execute() throws InterruptedException;

    protected BlockingQueue<TASK> queue_;
    protected long threadId_;
    protected String logPrefix_;
    protected boolean stop_;
}
