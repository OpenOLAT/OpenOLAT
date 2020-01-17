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
package org.olat.collaboration;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.VFSContextInfoResolver;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoImpl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 *  
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service
@Order(value=300)
public class CollaborationToolsFolderVFSContextInfoResolver implements VFSContextInfoResolver {
	private static final Logger log = Tracing.createLoggerFor(CollaborationToolsFolderVFSContextInfoResolver.class);
	
	@Autowired
	private BusinessGroupService bgService;


	@Override
	public String resolveContextTypeName(String vfsMetadataRelativePath, Locale locale) {
		if (vfsMetadataRelativePath == null) {
			return null;
		}
		String type = null;
		// Is either a transcoding or the master video
		if (vfsMetadataRelativePath.startsWith("cts")) {
			if (vfsMetadataRelativePath.startsWith("cts/folders")) {
				type = Util.createPackageTranslator(CollaborationToolsFolderVFSContextInfoResolver.class, locale).translate("vfs.context.cts.folders");
			} else if (vfsMetadataRelativePath.startsWith("cts/wikis")) {
				type = Util.createPackageTranslator(CollaborationToolsFolderVFSContextInfoResolver.class, locale).translate("vfs.context.cts.wikis");
			}
		}		
		return type;	
	}

	@Override
	public VFSContextInfo resolveContextInfo(String vfsMetadataRelativePath, Locale locale) {
		String type = resolveContextTypeName(vfsMetadataRelativePath, locale);
		if (type == null) {
			return null;
		}
		
		// Try finding detail infos. Path looks something like this cts/[tooltype]/BusinessGroup/[groupid]/...
		String name = "Unknown";
		String url = null;
				
		String[] path = vfsMetadataRelativePath.split("/");		
		if (path.length < 4) {
			return null; // no idea
		}
		String keyString = path[3];	
		// lookup group resource
		if (StringHelper.isLong(keyString)) {

			BusinessGroup businessGroup = bgService.loadBusinessGroup(Long.valueOf(keyString));
			
			if (businessGroup == null) {
				log.warn("No group found for id::" + keyString + " for path::" + vfsMetadataRelativePath);
			} else {
				name = businessGroup.getName();
				BusinessControlFactory bcf = BusinessControlFactory.getInstance();
				List<ContextEntry> entries = bcf.createCEListFromString("[BusinessGroup:" + businessGroup.getKey() + "]");					
				if (path[1].equals("folders")) {
					entries.addAll(bcf.createCEListFromString("[toolfolder:0]"));					
					// TODO: add other path elements to subdirectory
				} else if (path[1].equals("wikis")) {
					entries.addAll(bcf.createCEListFromString("[wiki:0]"));					
				}
				url = bcf.getAsURIString(entries, true);
			}
		} else {
			log.warn("Can not parse group id for path::{}", vfsMetadataRelativePath);
		}
		return new VFSContextInfoImpl(type, name, url);	
	}

}
