package concurrent;

import java.lang.Thread;
import concurrent.IWorkersFactory;

public abstract class WorkersFactory<TASK> implements IWorkersFactory<TASK> {
    public long getUniqueId() {
        return ++count_;
    }

    private long count_ = 0;
}