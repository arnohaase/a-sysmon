package com.ajjpj.asysmon.demo;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class creates a deadlock between two threads, just to showcase the monitoring of deadlocks.
 *
 * @author arno
 */
public class DeadlockThread extends Thread {
    final Object LOCK_A = new Object();
    final Object LOCK_B = new Object();

    @Override public void run() {
        synchronized(LOCK_A) {
            final Runnable r = new Runnable() {
                @Override public void run() {
                    synchronized(LOCK_B) {
                        synchronized(LOCK_A) {
                            System.out.println("..."); // should never happen
                        }
                    }
                }
            };

            final Runnable wrapped = (Runnable) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Runnable.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(r, args);
                }
            });

            new Thread(wrapped).start();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            synchronized(LOCK_B) {
                System.out.println("..."); // should never happen
            }
        }
    }
}
