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
import java.nio.file.Path;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  13.11.2002
 *
 * @author Mike Stock
 */
@Service
public class FolderModule extends AbstractSpringModule {	
	
	private static final Logger log = Tracing.createLoggerFor(FolderModule.class);

	private static final String CONFIG_FORCE_DOWNLOAD = "forceDownload";

	@Value("${folder.root}")
	private String homesRoot;
	@Value("${folder.editFileSizeLimitBytes:524288}")
	private int maxEditSizeLimit;
	private static final String CONFIG_ZIPSELECTIONMAXSIZEMB = "MaxZipSelectionSizeMb";
	@Value("${folder.maxulmb}")
	private int maxULMB;
	@Value("${folder.quotamb}")
	private int quotaMB;
	@Value("${folder.sendDocumentLinkOnly:true}")
	private boolean sendDocLinkyOnly;
	@Value("${folder.sendDocumentToExtern:false}")
	private boolean sendDocToExtern;
	@Value("${folder.force.download:true}")
	private String forceDownload;
	
	@Autowired
	public FolderModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	protected void initDefaultProperties() {
		// Set folder root
		if(!StringHelper.containsNonWhitespace(homesRoot)) {
			homesRoot = new File(System.getProperty("java.io.tmpdir"), "olatdata/bcroot").getAbsolutePath();
		}
		if (Settings.isJUnitTest()) {
			// use dummy directory for junit testcases to not conflict with actual data
			// on current server. someone may start junit test and not realize that this
			// can have side effects to a running instance on the same server...
			FolderConfig.setFolderRoot(homesRoot + "_junittest");
		} else { 
			FolderConfig.setFolderRoot(homesRoot);
		}
		log.info("Folder root set to '" + FolderConfig.getCanonicalRoot() + "'.");
		
		FolderConfig.setMaxEditSizeLimit(maxEditSizeLimit);
		// Set maximum upload filesize
		FolderConfig.setLimitULKB(maxULMB * 1024);
		log.info("Maximum file upload size set to " + FolderConfig.getLimitULKB() + " KB.");
		// Set default quotas
		FolderConfig.setDefaultQuotaKB(quotaMB * 1024);
		log.info("Default user quota set to " + FolderConfig.getDefaultQuotaKB() + " KB.");
		//set default
		FolderConfig.setSendDocumentLinkOnly(sendDocLinkyOnly);
		//set default
		FolderConfig.setSendDocumentToExtern(sendDocToExtern);
		
		// create tmp directory
		new File(FolderConfig.getCanonicalTmpDir()).mkdirs();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public void init() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabled = getStringPropertyValue(CONFIG_FORCE_DOWNLOAD, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			forceDownload = enabled;
		}
	}

	public boolean isForceDownload() {
		return "true".equals(forceDownload);
	}

	public void setForceDownload(boolean enable) {
		String enabled = enable ? "true" : "false";
		setStringProperty(CONFIG_FORCE_DOWNLOAD, enabled, true);
	}
	
	public String getCanonicalRoot() {
		return FolderConfig.getCanonicalRoot();
	}
	
	public Path getCanonicalRootPath() {
		return FolderConfig.getCanonicalRootPath();
	}
	
	public String getCanonicalTmpDir() {
		return FolderConfig.getCanonicalTmpDir();
	}
}
