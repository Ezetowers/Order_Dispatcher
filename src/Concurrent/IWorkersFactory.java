package concurrent;

import java.lang.Thread;
import java.util.concurrent.BlockingQueue;

public interface IWorkersFactory<TASK> {
    public abstract Thread make();
    public abstract BlockingQueue<TASK> getQueue();
}