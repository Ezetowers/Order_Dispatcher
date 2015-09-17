package logger;

public enum LogLevel {
    ERROR(1), 
    CRITIC(2), 
    WARNING(3), 
    INFO(4), 
    NOTICE(5), 
    DEBUG(6), 
    TRACE(7);

    LogLevel(int level) {
        if (this.isLevelValid(level)) {
            this.level_ = level;
        }
        else {
            throw new IllegalArgumentException("LogLevel is out of bounds.");
        }
    }

    public static LogLevel parse(String level) {
        String lowerLogLevel = level.toLowerCase().trim();

        if (lowerLogLevel.equals("error")) {
            return LogLevel.ERROR;
        }
        else if (lowerLogLevel.equals("critic")) {
            return LogLevel.CRITIC;
        }
        else if (lowerLogLevel.equals("warning")) {
            return LogLevel.WARNING;
        }
        else if (lowerLogLevel.equals("info")) {
            return LogLevel.INFO;
        }
        else if (lowerLogLevel.equals("notice")) {
            return LogLevel.NOTICE;
        }
        else if (lowerLogLevel.equals("debug")) {
            return LogLevel.DEBUG;
        }
        else if (lowerLogLevel.equals("trace")) {
            return LogLevel.TRACE;
        }

        throw new IllegalArgumentException("Invalid LogLevel introduced: " 
            + level);
    }

    public int level() {
        return level_;
    }

    public Boolean isLevelValid(int level) {
        // TODO: This enum are shit. I try, but this is the best a can do
        return level >= 1 && level <= 7;
        /*return level >= LogLevel.ERROR && 
               level <= LogLevel.TRACE;*/
    }

    public String prefix(LogLevel verbosity) {
        int level = verbosity.level();

        if (this.isLevelValid(level)) {
            return PREFIX_ARRAY[level - 1];
        }

        throw new IllegalArgumentException("LogLevel is out of bounds.");
    }

    private final int level_;
    private final String PREFIX_ARRAY[] = {"[ERROR]",
                                           "[CRITIC]",
                                           "[WARNING]",
                                           "[INFO]",
                                           "[NOTICE]",
                                           "[DEBUG]",
                                           "[TRACE]"};
}
