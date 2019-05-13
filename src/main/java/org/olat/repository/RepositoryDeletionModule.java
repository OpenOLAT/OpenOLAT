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
import java.util.Date;

import org.olat.basesecurity.manager.IdentityDAO;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 15.06.2006 <br>
 * @author Christian Guretzki
 */
@Service("deletionModule")
public class RepositoryDeletionModule implements InitializingBean {
	private static final Logger log = Tracing.createLoggerFor(RepositoryDeletionModule.class);
	private static final String CONF_DELETE_EMAIL_RESPONSE_TO_USER_NAME = "deleteEmailResponseToUserName";
	private static final String DEFAULT_ADMIN_USERNAME = "administrator";
	
	@Value("${archive.dir}")
	private String archiveRootPath;
	@Value("${deleteEmailResponseToUserName:administrator}")
	private String emailResponseTo;
	@Value("${deletionModule.adminUserName:administrator}")
	private String adminUserName;
	
	@Autowired
	private IdentityDAO identityDao;

	@Override
	public void afterPropertiesSet() {
		if(!StringHelper.containsNonWhitespace(archiveRootPath)) {
			archiveRootPath = Paths.get(System.getProperty("java.io.tmpdir"), "olatdata", "deleted_archive").toString();
		}
				
		if (!emailResponseTo.contains("@")) {
			Identity identity = identityDao.findIdentityByName(emailResponseTo);
			if (identity != null) {
				emailResponseTo = identity.getUser().getEmail();
			} else {
				log.warn("Could not find:  " + CONF_DELETE_EMAIL_RESPONSE_TO_USER_NAME + " with name: " + emailResponseTo);
				emailResponseTo = WebappHelper.getMailConfig("mailFrom");
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("archiveRootPath=" + archiveRootPath);
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
		Identity adminUserIdentity;
		if (adminUserName != null) {
			adminUserIdentity = identityDao.findIdentityByName(adminUserName);
		} else {
			adminUserIdentity = identityDao.findIdentityByName(DEFAULT_ADMIN_USERNAME);
		}
		return adminUserIdentity;
	}
}
