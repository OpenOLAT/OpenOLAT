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
package org.olat.admin.sysinfo.manager;

import java.io.File;

import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CustomStaticFolderManager implements InitializingBean, WebDAVProvider {

	private static final OLog log = Tracing.createLoggerFor(CustomStaticFolderManager.class);
	public static final String STATIC_FOLDER = "/customizing/static/";

	private static final String MOUNT_POINT = "customizing";
	
	@Override
	public void afterPropertiesSet() throws Exception {
		File file = new File(WebappHelper.getUserDataRoot(), STATIC_FOLDER);
		if(!file.exists() && !file.mkdirs()) {
			log.error("/customizing/static/ folder cannot be created");
		}
	}

	public VFSContainer getRootContainer() {
		File file = new File(WebappHelper.getUserDataRoot(), STATIC_FOLDER);
		VFSContainer rootContainer = new LocalFolderImpl(file);
		rootContainer.setLocalSecurityCallback(new FullAccessCallback());
		return rootContainer;
	}
	
	public File getRootFile() {
		return new File(WebappHelper.getUserDataRoot(), STATIC_FOLDER);
	}

	@Override
	public String getMountPoint() {
		return MOUNT_POINT;
	}
	
	@Override
	public boolean hasAccess(IdentityEnvironment identityEnv) {
		return identityEnv != null && identityEnv.getRoles() != null
				&& (identityEnv.getRoles().isAdministrator() || identityEnv.getRoles().isSystemAdmin());
	}

	@Override
	public VFSContainer getContainer(IdentityEnvironment identityEnv) {
		if(identityEnv != null && identityEnv.getRoles() != null
				&& (identityEnv.getRoles().isAdministrator() || identityEnv.getRoles().isSystemAdmin())) {
			return getRootContainer();
		}
		return null;
	}
}