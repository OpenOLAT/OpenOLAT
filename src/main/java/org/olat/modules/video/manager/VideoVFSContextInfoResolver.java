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
package org.olat.modules.video.manager;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryVFSContextInfoResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * A specialized version of the repository entry resolver that catches also the
 * transcoded video resources. This resolver has a higher order value to be
 * executed before the default repository resolver.
 * <br />
 * The class inheritence is not necessary, it just shows that this belongs to the repository
 * 
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service
@Order(value=200)
public class VideoVFSContextInfoResolver extends RepositoryEntryVFSContextInfoResolver {
	private static final Logger log = Tracing.createLoggerFor(VideoVFSContextInfoResolver.class);

	@Override
	public String resolveContextTypeName(String vfsMetadataRelativePath, Locale locale) {
		if (vfsMetadataRelativePath == null) {
			return null;
		}
		String type = null;
		// Is either a transcoding or the master video
		if (vfsMetadataRelativePath.startsWith("transcodedVideos")) {
			type = Util.createPackageTranslator(VideoDisplayController.class, locale).translate("admin.menu.transcoding.title");
		} else if (vfsMetadataRelativePath.startsWith("repository") && vfsMetadataRelativePath.endsWith("master")) {
			type = Util.createPackageTranslator(VideoDisplayController.class, locale).translate("quality.master");
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
				if (re == null) {
					log.warn("No repository entry found for key::" + keyString + " for path::" + vfsMetadataRelativePath);
				} else {
					name = re.getDisplayname();
					url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + re.getKey();
				}
			}
		} else {
			log.warn("Can not parse repo entry id for path::{}", vfsMetadataRelativePath);
		}
		
		return new VFSContextInfoImpl(type, name, url);	
	}

}
