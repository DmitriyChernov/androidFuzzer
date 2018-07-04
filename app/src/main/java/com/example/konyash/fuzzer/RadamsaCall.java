package com.example.konyash.fuzzer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class RadamsaCall implements Callable {
    MainActivity mainActivity;
    String options;
    String input;

    public RadamsaCall(MainActivity mainActivity, String options, String input) {
        this.mainActivity = mainActivity;
        this.options = options;
        this.input = input;
    }

    @Override
    public Integer call() {
        TCP_Client radamsa = new TCP_Client(mainActivity);
        radamsa.execute(options, input);

        Integer respCount = 0;

        do {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() {
                    return new Integer(MainActivity.responceCount());
                }
            };
            Future<Integer> future = executor.submit(callable);
            try {
                respCount = future.get();
            }  catch (Exception e)
            {
                respCount = 0;
            }
            executor.shutdown();

        } while (respCount == 0);


        return respCount;
    }
}