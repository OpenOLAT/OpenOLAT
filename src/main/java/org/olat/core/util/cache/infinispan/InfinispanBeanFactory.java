package org.olat.core.util.cache.infinispan;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

/**
 * Initial date: 2016-01-13<br />
 * @author sev26 (UZH)
 */
@Configuration
public class InfinispanBeanFactory {

	@Bean(name = DefaultCacheManager.OBJECT_NAME)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public EmbeddedCacheManager getEmbeddedCacheManager(String configurationFile) throws IOException {
		return new DefaultCacheManager(configurationFile);
	}
}
