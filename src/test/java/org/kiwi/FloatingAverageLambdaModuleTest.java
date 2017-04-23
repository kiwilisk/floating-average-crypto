package org.kiwi;

import static com.google.inject.Guice.createInjector;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Injector;
import org.junit.Test;
import org.kiwi.config.ConfigurationLoader;

public class FloatingAverageLambdaModuleTest {

    @Test
    public void should_load_module() throws Exception {
        Injector injector = createInjector(new FloatingAverageLambdaModule());

        injector.getInstance(ConfigurationLoader.class);
        assertThat(injector).isNotNull();
    }
}