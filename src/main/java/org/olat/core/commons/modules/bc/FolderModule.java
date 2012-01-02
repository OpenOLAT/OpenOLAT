/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import java.io.File;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.version.FolderVersioningConfigurator;

/**
 * Initial Date:  13.11.2002
 *
 * @author Mike Stock
 */
public class FolderModule extends AbstractOLATModule {	
	
	OLog log = Tracing.createLoggerFor(FolderModule.class);
	private static final String CONFIG_ROOT = "Root";
	private static final String CONFIG_LIMITULMB = "LimitULMB";
	private static final String CONFIG_QUOTAMB = "QuotaMB";
	private static final String CONFIG_SENDDOCLINKONLY = "SendDocLinkOnly";
	private static final String CONFIG_SENDDOCTOEXTERN = "SendDocToExtern";
	private FolderVersioningConfigurator versioning;
	
	/**
	 * [used by spring]
	 */
	private FolderModule() {
		//
	}

	@Override
	protected void initDefaultProperties() {
		// Set folder root
		File defaultHome = new File(System.getProperty("java.io.tmpdir"), "olatdata/bcroot");
		String homesRoot = getStringConfigParameter(CONFIG_ROOT, defaultHome.getAbsolutePath() , false);
		if (Settings.isJUnitTest()) {
			// use dummy directory for junit testcases to not conflict with actual data
			// on current server. someone may start junit test and not realize that this
			// can have side effects to a running instance on the same server...
			FolderConfig.setFolderRoot(homesRoot + "_junittest");
		} else { 
			FolderConfig.setFolderRoot(homesRoot);
		}
		
		log.info("Folder root set to '" + FolderConfig.getCanonicalRoot() + "'.");
		
		// Set maximum upload filesize
		int maxULMB =getIntConfigParameter(CONFIG_LIMITULMB, 100);
		FolderConfig.setLimitULKB(maxULMB * 1024);
		
		log.info("Maximum file upload size set to " + FolderConfig.getLimitULKB() + " KB.");
		
		// Set default quotas
		int quotaMB = getIntConfigParameter(CONFIG_QUOTAMB, 100);
		FolderConfig.setDefaultQuotaKB(quotaMB * 1024);
		log.info("Default user quota set to " + FolderConfig.getDefaultQuotaKB() + " KB.");
		
		//set default
		boolean sendDocLinkyOnly = getBooleanConfigParameter(CONFIG_SENDDOCLINKONLY, true);
		FolderConfig.setSendDocumentLinkOnly(sendDocLinkyOnly);
		
		//set default
		boolean sendDocToExtern = getBooleanConfigParameter(CONFIG_SENDDOCTOEXTERN, false);
		FolderConfig.setSendDocumentToExtern(sendDocToExtern);
		
		// create tmp directory
		File fTmp = new File(FolderConfig.getCanonicalTmpDir());
		fTmp.mkdirs();
		
	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		FolderConfig.setVersioningConfigurator(versioning);
	}

	/**
	 * [used by spring]
	 * @param versioning
	 */
	public void setVersioning(FolderVersioningConfigurator versioning) {
		this.versioning = versioning;
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
	

}
