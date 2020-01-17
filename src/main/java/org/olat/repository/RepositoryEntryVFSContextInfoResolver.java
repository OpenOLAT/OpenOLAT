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
package org.olat.repository;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.VFSContextInfoResolver;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A generic repository resolver that catches everything that has not been
 * resolved by a more specific resolver with the higher order
 * 
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Component
@Order(value=10000)
public class RepositoryEntryVFSContextInfoResolver implements VFSContextInfoResolver {
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryVFSContextInfoResolver.class);

	@Autowired
	protected RepositoryService repositoryService;

	@Override
	public String resolveContextTypeName(String vfsMetadataRelativePath, Locale locale) {
		if (vfsMetadataRelativePath == null) {
			return null;
		}
		String type = null;
		// Catch all repo entry path
		if (vfsMetadataRelativePath.startsWith("repository")){
			type = Util.createPackageTranslator(RepositoryEntryVFSContextInfoResolver.class, locale).translate("vfs.context.repositoryentry");
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
		if (StringHelper.isLong(keyString)) {

			List<RepositoryEntry> repoEntries = repositoryService.searchByIdAndRefs(keyString);
			if (repoEntries.size() != 1) {
				log.warn("No olat resource resource found for id::" + keyString + " for path::" + vfsMetadataRelativePath);
			} else {
				RepositoryEntry re = repoEntries.get(0);
				name = re.getDisplayname();
				url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + re.getKey();
				type = Util.createPackageTranslator(RepositoryEntryVFSContextInfoResolver.class, locale).translate(re.getOlatResource().getResourceableTypeName());
			}
		} else {
			log.warn("Can not parse repo entry id for path::{}", vfsMetadataRelativePath);
		}
		
		return new VFSContextInfoImpl(type, name, url);	
	}

}
