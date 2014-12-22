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
*/

package org.olat.repository;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 15.06.2006 <br>
 * @author Christian Guretzki
 */
@Service("deletionModule")
public class RepositoryDeletionModule extends AbstractSpringModule {
	private static final OLog log = Tracing.createLoggerFor(RepositoryDeletionModule.class);
	private static final String CONF_DELETE_EMAIL_RESPONSE_TO_USER_NAME = "deleteEmailResponseToUserName";
	private static final String DEFAULT_ADMIN_USERNAME = "administrator";
	
	@Value("${archive.dir}")
	private String archiveRootPath;
	@Value("${deleteEmailResponseToUserName:administrator}")
	private String emailResponseTo;
	@Value("${deletionModule.adminUserName:administrator}")
	private String adminUserName;

	private Identity adminUserIdentity;
	@Autowired
	private WebappHelper webappHelper;
	@Autowired
	private BaseSecurity baseSecurityManager;

	@Autowired
	public RepositoryDeletionModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public void init() {
		if(!StringHelper.containsNonWhitespace(archiveRootPath)) {
			archiveRootPath = Paths.get(System.getProperty("java.io.tmpdir"), "olatdata", "deleted_archive").toString();
		}
				
		if (!emailResponseTo.contains("@")) {
			List<IdentityShort> identities = baseSecurityManager.findShortIdentitiesByName(Collections.singletonList(emailResponseTo));
			if (identities != null && identities.size() == 1) {
				emailResponseTo = identities.get(0).getEmail();
			} else {
				log.warn("Could not find:  " + CONF_DELETE_EMAIL_RESPONSE_TO_USER_NAME + " with name: " + emailResponseTo, null);
				emailResponseTo = WebappHelper.getMailConfig("mailFrom");
			}
		}

		if (adminUserName != null) {
			adminUserIdentity = baseSecurityManager.findIdentityByName(adminUserName);
		} else {
			adminUserIdentity = baseSecurityManager.findIdentityByName(DEFAULT_ADMIN_USERNAME);
		}
		
		if(log.isDebug()) {
			log.debug("archiveRootPath=" + archiveRootPath);
			log.debug("emailResponseTo=" + emailResponseTo);
			log.debug("adminUserIdentity=" + adminUserIdentity);
		}
	}

	/**
	 * @return Returns the archiveRootPath.
	 */
	public String getArchiveRootPath() {
		return archiveRootPath;
	}

	/**
	 * @return Returns the deleteEmailFrom.
	 */
	public String getEmailResponseTo() {
		return emailResponseTo;
	}

	public static String getArchiveDatePath() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(new Date());
	}

	public Identity getAdminUserIdentity() {
		return adminUserIdentity;
	}
}
