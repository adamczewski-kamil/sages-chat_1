package pl.chat.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger logger = Logger.getLogger(Thread.currentThread().getName());
        logger.log(Level.SEVERE, e.toString());
        e.printStackTrace();
    }

}
