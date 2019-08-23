/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.cache.infinispan;

import javax.naming.InitialContext;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * The embbeded cache manager of infinispan
 * 
 * 
 * Initial date: 17.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfinispanCacheManager implements FactoryBean<EmbeddedCacheManager> {
	
	private String configuration;
	private String jndiName;
	private EmbeddedCacheManager cacheManager;

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * [used by Spring]
	 * @param configuration
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}
	
	public void stop() {
		if(!StringHelper.containsNonWhitespace(jndiName)) {
			cacheManager.stop();
		}
	}

	@Override
	public EmbeddedCacheManager getObject() throws Exception {
		if(cacheManager == null) {
			if(StringHelper.containsNonWhitespace(jndiName)) {
				InitialContext ctx = new InitialContext();
				cacheManager = (EmbeddedCacheManager)ctx.lookup(jndiName);
			} else {
				if(!StringHelper.containsNonWhitespace(configuration)) {
					configuration = "infinispan-config.xml";
				}
				cacheManager = (EmbeddedCacheManager) applicationContext
						.getBean(DefaultCacheManager.OBJECT_NAME, configuration);
				cacheManager.start();
			}
		}
		return cacheManager;
	}

	@Override
	public Class<?> getObjectType() {
		return EmbeddedCacheManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
