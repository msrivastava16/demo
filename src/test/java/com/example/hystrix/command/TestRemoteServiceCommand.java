package com.example.hystrix.command;

import com.example.hystrix.com.example.hystrix.command.RemoteServiceCommand;
import com.example.hystrix.simulator.RemoteServiceSimulator;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRemoteServiceCommand {

    @Test
    public void givenServiceTimeoutOf100SecWhenRemoteServiceExecuted_thenReturnSuccess()
            throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroup2"));
        RemoteServiceCommand command = new RemoteServiceCommand(config, new RemoteServiceSimulator(100));
        assertThat(command.execute()).isEqualTo("Success");
    }

    @Test
    public void givenServiceTimeoutOf15000SecAndExecutionTimeoutOf5000_whenRemoteServiceExecuted_thenReturnSuccess()
            throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupTest4"));
        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(5000);
        config.andCommandPropertiesDefaults(commandProperties);
        RemoteServiceCommand command = new RemoteServiceCommand(config, new RemoteServiceSimulator(15000));
        assertThat(command.execute()).isEqualTo("Success");
    }

    @Test
    public void givenServiceTimeoutOf500AndExecTimeoutOf10000AndThreadPool_whenRemoteServiceExecuted_thenReturnSuccess() throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupThreadPool"));
        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(10000);
        config.andCommandPropertiesDefaults(commandProperties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(10)
                .withCoreSize(3)
                .withQueueSizeRejectionThreshold(10));
        RemoteServiceCommand command = new RemoteServiceCommand(config, new RemoteServiceSimulator(500));
        assertThat(command.execute()).isEqualTo("Success");
    }

    @Test
    public void givenCircuitBreakerSetup_whenRemoteServiceCmdExecuted_thenReturnSuccess()
            throws InterruptedException {
        HystrixCommand.Setter config = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceGroupCircuitBreaker"));
        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        properties.withExecutionTimeoutInMilliseconds(1000);
        properties.withCircuitBreakerSleepWindowInMilliseconds(4000);
        properties.withExecutionIsolationStrategy
                (HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withCircuitBreakerEnabled(true);
        properties.withCircuitBreakerRequestVolumeThreshold(1);
        config.andCommandPropertiesDefaults(properties);
        config.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withMaxQueueSize(1)
                .withCoreSize(1)
                .withQueueSizeRejectionThreshold(1));
        assertThat(this.invokeRemoteService(config, 10000)).isEqualTo(null);
        assertThat(this.invokeRemoteService(config, 10000)).isEqualTo(null);
        assertThat(this.invokeRemoteService(config, 10000)).isEqualTo(null);
        Thread.sleep(5000);
        assertThat((new RemoteServiceCommand(config, new RemoteServiceSimulator(500)).execute())).isEqualTo("Success");
        assertThat((new RemoteServiceCommand(config, new RemoteServiceSimulator(500)).execute())).isEqualTo("Success");
        assertThat((new RemoteServiceCommand(config, new RemoteServiceSimulator(500)).execute())).isEqualTo("Success");
    }

    public String invokeRemoteService(HystrixCommand.Setter config, int timeout)
            throws InterruptedException {
        String response = null;
        try {
            response = new RemoteServiceCommand(config,
                    new RemoteServiceSimulator(timeout)).execute();
        } catch (HystrixRuntimeException ex) {
            System.out.println("ex = " + ex);
        }
        return response;
    }
}
