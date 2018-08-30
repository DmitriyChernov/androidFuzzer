package com.example.konyash.fuzzer.generator;

import com.example.konyash.fuzzer.fuzzer.Fuzzer;
import com.example.konyash.fuzzer.fuzzer.IFuzzer;
import com.example.konyash.fuzzer.main.MainActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RadamsaCall implements Callable {

    private final IFuzzer fuzzer;
    private final String options;
    private final String input;

    public RadamsaCall(IFuzzer fuzzer, String options, String input) {
        this.fuzzer = fuzzer;
        this.options = options;
        this.input = input;
    }

    @Override
    public Integer call() {
        TCP_Client radamsa = new TCP_Client(fuzzer);
        radamsa.execute(options, input);

        Integer respCount = 0;

        do {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() {
                    return new Integer(fuzzer.getResponseQueueSize());
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