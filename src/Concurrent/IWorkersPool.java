package concurrent;

public interface IWorkersPool {
    public abstract void start();
    public abstract void stop();
    public abstract void join();
}