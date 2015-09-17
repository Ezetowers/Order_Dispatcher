package concurrent;

import concurrent.WorkersFactory;
import concurrent.IWorkersPool;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import logger.Logger;
import logger.LogLevel;

public class WorkersPool<TASK> implements IWorkersPool {
    public WorkersPool(int amountWorkers, WorkersFactory<TASK> factory) {
        factory_ = factory;
        workers_ = new ArrayList<Thread>();
        queue_ = factory.getQueue();
        amountWorkers_ = amountWorkers;

        for (int i = 0; i < amountWorkers_; ++i) {
            // Initialize Workers Pool
            Thread worker = factory_.make();
            workers_.add(worker);
        }
    }

    public void start() {
        for (Thread worker : workers_) {
            worker.start();
        }
    }

    public void stop() {
        for (Thread worker : workers_) {
            worker.interrupt();
        }
    }

    public void join() {
        for (Thread worker : workers_) {
            try {
                worker.join();
            }
            catch (InterruptedException e) {
            }
        }
    }

    public Boolean addTask(TASK task) {
        // TODO: Check if the queue is full
        queue_.add(task);
        return true;
    }


    private int amountWorkers_;
    private WorkersFactory<TASK> factory_;
    private ArrayList<Thread> workers_;
    private BlockingQueue<TASK> queue_;
}
