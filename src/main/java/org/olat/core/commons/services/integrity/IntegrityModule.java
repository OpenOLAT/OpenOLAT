/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.integrity;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.admin.AdminModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The module checks the presence of a token in the database and on the file
 * system. If the 2 tokens doesn't match, or if one exists and the other not,
 * a log entry is written. If the hard check is enabled with the property,
 * an exception will interrupt the startup process.<br>
 * 
 * 
 * Initial date: 2 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class IntegrityModule extends AbstractSpringModule {

	private static final Logger log = Tracing.createLoggerFor(IntegrityModule.class);
	
	private static final String INTEGRITY_FS_DB = "integrityFsDb";
	private static final String INTEGRITY_FILENAME = "integrity.txt";
	
	@Value("${integrity.fs.db.enable:false}")
	private boolean enabled;
	
	private final DB dbInstance;
	private final WebappHelper webappHelper;
	private final PropertyManager propertyManager;
	
	public IntegrityModule(CoordinatorManager coordinatorManager, PropertyManager propertyManager, WebappHelper webappHelper, DB dbInstance) {
		super(coordinatorManager);
		this.dbInstance = dbInstance;
		this.webappHelper = webappHelper;
		this.propertyManager = propertyManager;
	}

	@Override
	public void init() {
		check(enabled);
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}
	
	public Check check(boolean hardCheck) {
		Check check = Check.OK;
		Property p = propertyManager.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, INTEGRITY_FS_DB);
		@SuppressWarnings("static-access")
		String userDataDirectory = webappHelper.getUserDataRoot();
		File integrityFile = Paths.get(userDataDirectory, "system", INTEGRITY_FILENAME).toFile();
		if(p == null && !integrityFile.exists()) {
			String token = UUID.randomUUID().toString();
			p = propertyManager.createPropertyInstance(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, INTEGRITY_FS_DB, null, null, token, null);
			propertyManager.saveProperty(p);
			FileUtils.save(integrityFile, token, "UTF-8");
			log.info("Create integrity token file system / database");
			check = Check.INITIALIZED;
		} else if(p != null && integrityFile.exists()) {
			String dbToken = p.getStringValue();
			String fsToken = FileUtils.load(integrityFile, "UTF-8");
			if(dbToken != null && dbToken.equals(fsToken)) {
				check = Check.OK;
				log.info("Integrity file system / database verified");
			} else {
				check = Check.FAIL;
				log.fatal("Integrity file system / database verification failed, token missmatch");
				if(hardCheck) {
					throw new IllegalStateException("Integrity file system database verification failed");
				}
			}
		} else if(p == null) {
			log.fatal("Integrity file system / database verification failed, database token missing");
			if(hardCheck) {
				throw new IllegalStateException("Integrity file system database verification failed, database token missing");
			}
			check = Check.FAIL;
		 } else if(!integrityFile.exists()) {
			log.fatal("Integrity file system / database verification failed, file token missing");
			if(hardCheck) {
				throw new IllegalStateException("Integrity file system database verification failed, file token missing");
			}
			check = Check.FAIL;
		}
		dbInstance.commitAndCloseSession();
		return check;
	}
	
	public enum Check {
		OK,
		FAIL,
		INITIALIZED
	}
}
