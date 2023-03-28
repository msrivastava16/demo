package com.example.hystrix.com.example.hystrix.command;

import com.example.hystrix.simulator.RemoteServiceSimulator;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

public class RemoteServiceCommand extends HystrixCommand<String> {

    private RemoteServiceSimulator remoteService;

    public RemoteServiceCommand(Setter config, RemoteServiceSimulator remoteService) {
        super(config);
        this.remoteService = remoteService;
    }

    @Override
    protected String run() throws Exception {
        return remoteService.execute();
    }


}