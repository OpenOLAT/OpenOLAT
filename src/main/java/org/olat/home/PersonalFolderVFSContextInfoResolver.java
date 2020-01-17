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
package org.olat.home;

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.VFSContextInfoResolver;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 *  
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Component
@Order(value=400)
public class PersonalFolderVFSContextInfoResolver implements VFSContextInfoResolver {
	private static final Logger log = Tracing.createLoggerFor(PersonalFolderVFSContextInfoResolver.class);

	@Autowired
	private BaseSecurityManager baseSecurityMgr;

	@Override
	public String resolveContextTypeName(String vfsMetadataRelativePath, Locale locale) {
		if (vfsMetadataRelativePath == null) {
			return null;
		}
		String type = null;
		// Is either a transcoding or the master video
		if (vfsMetadataRelativePath.startsWith("homes")) {
			type = Util.createPackageTranslator(PersonalFolderVFSContextInfoResolver.class, locale).translate("vfs.context.homes");
		}		
		return type;	
	}

	@Override
	public VFSContextInfo resolveContextInfo(String vfsMetadataRelativePath, Locale locale) {
		String type = resolveContextTypeName(vfsMetadataRelativePath, locale);
		if (type == null) {
			return null;
		}
		
		// Try finding detail infos
		String name = "Unknown";
		String url = null;
				
		String[] path = vfsMetadataRelativePath.split("/");		
		String keyString = path[1];						
		Identity identity = baseSecurityMgr.findIdentityByName(keyString);
		if (identity == null) {
			log.warn("No identity found for id::{} for path::{}", keyString, vfsMetadataRelativePath);
		} else {
			name = UserManager.getInstance().getUserDisplayName(identity); 
			url = Settings.getServerContextPathURI() + "/auth/HomeSite/" + identity.getKey() + "/userfolder/0";
			// TODO: add other path elements to subdirectory

		}
		
		return new VFSContextInfoImpl(type, name, url);	
	}

}
