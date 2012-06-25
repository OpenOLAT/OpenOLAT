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
