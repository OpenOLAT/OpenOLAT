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

package org.olat.core.commons.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * 
 * Description:<br>
 * Extends the Spring LocalSessionFactoryBean to allow additional mappings
 * for Hibernate. The additional mappings are defined in beans of the 
 * org.olat.core.commons.persistence.AdditionalDBMappings. These beans must be
 * <strong>singleton</strong> and not use lazy-init.
 * <P>
 * 	&lt;bean id="markingMapping" class="org.olat.core.commons.persistence.AdditionalDBMappings"&gt;
 *		&lt;property name="xmlFiles"&gt;
 *			&lt;list&gt;
 *				&lt;value&gt;org/olat/core/commons/services/mark/impl/MarkImpl.hbm.xml&lt;/value&gt;
 *			&lt;/list&gt;
 *		&lt;/property&gt;
 *	&lt;/bean&gt;
 * 
 * <P>
 * Initial Date:  23 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OLATLocalSessionFactoryBean extends LocalSessionFactoryBean {
	
	private OLog log = Tracing.createLoggerFor(OLATLocalSessionFactoryBean.class);
	
	private AdditionalDBMappings[] additionalDBMappings;

	public void setAdditionalDBMappings(AdditionalDBMappings[] additionalDBMappings) {
		this.additionalDBMappings = additionalDBMappings;
	}

	@Override
	protected void postProcessMappings(Configuration config) throws HibernateException {
		try {
			if(additionalDBMappings != null && additionalDBMappings.length > 0) {
				for(AdditionalDBMappings addMapping:additionalDBMappings) {
					List<String> xmlFiles = addMapping.getXmlFiles();
					if(xmlFiles != null) {
						for (String mapping : xmlFiles) {
							//we cannot access the classloader magic used by the LocalSessionFactoryBean
							Resource resource = new ClassPathResource(mapping.trim());
							config.addInputStream(resource.getInputStream());
						}
					}
					
					List<Class<?>> annotatedClasses = addMapping.getAnnotatedClasses();
					if(annotatedClasses != null) {
						for(Class<?> annotatedClass:annotatedClasses) {
							config.addClass(annotatedClass);
						}
					}
				}
			}
			super.postProcessMappings(config);
		} catch (Exception e) {
			log.error("Error during the post processing of the hibernate session factory.", e);
		}
	}
}
