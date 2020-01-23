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
package org.olat.course;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryVFSContextInfoResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * 
 * A specialized version of the repository entry resolver that catches the
 * course resources. Since courses are special, they are not located in the
 * standard repository directory but have their own. 
 * <br />
 * The class inheritence is not necessary, it just shows that this belongs to
 * the repository.
 * 
 * 
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Service
@Order(value=100)
public class CourseVFSContextInfoResolver extends RepositoryEntryVFSContextInfoResolver {
	private static final Logger log = Tracing.createLoggerFor(CourseVFSContextInfoResolver.class);

	@Override
	public String resolveContextTypeName(String vfsMetadataRelativePath, Locale locale) {
		if (vfsMetadataRelativePath == null) {
			return null;
		}
		String type = null;
		if (vfsMetadataRelativePath.startsWith(PersistingCourseImpl.COURSE_ROOT_DIR_NAME)) {
			if (vfsMetadataRelativePath.contains(PersistingCourseImpl.COURSEFOLDER)) {
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.coursefolder");
			} else if (vfsMetadataRelativePath.contains("foldernodes")) {
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.foldernodes");
			} else if (vfsMetadataRelativePath.contains("gtasks")) {
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.gtasks");
			} else if (vfsMetadataRelativePath.contains("export")) {
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.export");
			} else if (vfsMetadataRelativePath.contains("participantfolder")) {
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.participantfolder");
			} else { 
				type = Util.createPackageTranslator(CourseVFSContextInfoResolver.class, locale).translate("vfs.context.courseconfiguration");
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
		
		// Try finding detail infos
		String name = "Unknown";
		String url = null;
				
		String[] path = vfsMetadataRelativePath.split("/");		
		String keyString = path[1];						
		if (StringHelper.isLong(keyString)) {
			List<RepositoryEntry> repoEntries = repositoryService.searchByIdAndRefs(keyString);
			if (repoEntries.size() != 1) {
				log.warn("No olat resource resource found for id::{} for path::{}", keyString, vfsMetadataRelativePath);
			} else {
				RepositoryEntry re = repoEntries.get(0);
				if (re == null) {
					log.warn("No repository entry found for key::{} for path::{}", keyString, vfsMetadataRelativePath);
				} else {
					name = re.getDisplayname();
					url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + re.getKey();					
					
					if (path.length >= 4 && PersistingCourseImpl.COURSEFOLDER.equals(path[2])) {
						// Add direct path to course folder
						// TODO: add other path elements to subdirectory
						url += "/path%3D~~/0";
					} else if (path.length >= 4 && ("foldernodes".equals(path[2]) || "gtasks".equals(path[2]) || "participantfolder".equals(path[2]))) {
						// Add course node jump in if available
						url += "/CourseNode/" + path[3];
					}
				}
			}
		} else {
			log.warn("Can not parse repo entry id for path::{}", vfsMetadataRelativePath);
		}
		
		return new VFSContextInfoImpl(type, name, url);	
	}

}
