package id.ac.ui.cs.advprog.palmerymanage.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void asyncExecutor_ReturnsConfiguredThreadPool() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.asyncExecutor();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(2, taskExecutor.getCorePoolSize());
        assertEquals(5, taskExecutor.getMaxPoolSize());
        assertEquals("HarvestAsync-", taskExecutor.getThreadNamePrefix());
    }
}
