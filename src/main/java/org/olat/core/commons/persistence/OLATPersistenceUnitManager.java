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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OLATPersistenceUnitManager extends DefaultPersistenceUnitManager {

	private OLog log = Tracing.createLoggerFor(OLATPersistenceUnitManager.class);
	
	private AdditionalDBMappings[] additionalDBMappings;

	public void setAdditionalDBMappings(AdditionalDBMappings[] additionalDBMappings) {
		this.additionalDBMappings = additionalDBMappings;
	}

	@Override
	protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
		try {
			if(additionalDBMappings != null && additionalDBMappings.length > 0) {
				for(AdditionalDBMappings addMapping:additionalDBMappings) {
					List<String> xmlFiles = addMapping.getXmlFiles();
					if(xmlFiles != null) {
						for (String mapping : xmlFiles) {
							pui.addMappingFileName(mapping.trim());
						}
					}
					
					List<Class<?>> annotatedClasses = addMapping.getAnnotatedClasses();
					if(annotatedClasses != null) {
						for(Class<?> annotatedClass:annotatedClasses) {
							pui.addManagedClassName(annotatedClass.getName());
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error during the post processing of the hibernate session factory.", e);
		}
		
		super.postProcessPersistenceUnitInfo(pui);
	}

}
