/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.webdav;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyArchivesWebDAVSource extends MergeSource {

	private static final Logger log = Tracing.createLoggerFor(CourseArchiveWebDAVSource.class);
	
	private List<VFSItem> exports;
	private boolean initialized = false;
	private final IdentityEnvironment identityEnv;
	
	public MyArchivesWebDAVSource(IdentityEnvironment identityEnv) {
		super(null, "mycoursearchives");
		this.identityEnv = identityEnv;
	}
	
	public boolean isEmpty() {
		if(!initialized) {
			init();
		}
		return exports == null || exports.isEmpty();
	}

	@Override
	public VFSItem resolve(String path) {
		if(!initialized) {
			init();
		}
		
		VFSItem item = super.resolve(path);
		if(item == null) {
			path = VFSManager.sanitizePath(path);
			String childName = VFSManager.extractChild(path);
			for(VFSItem export:exports) {
				if(export.getName().equals(childName)) {
					item = export;
					break;
				}
			}
		}
		return item;
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			init();
		}
		return exports;
	}
	
	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}

	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		return getItems(filter);
	}

	@Override
	protected void init() {
		if(!initialized) {
			exports = getExports();
			initialized = true;
		}
		super.init();
	}
	
	private List<VFSItem> getExports() {
		try {
			ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
			SearchExportMetadataParameters params = new SearchExportMetadataParameters(List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL));
			if(identityEnv != null && identityEnv.getRoles() != null) {
				Roles roles = identityEnv.getRoles();
				boolean hasAdministrativeRoles = roles.hasSomeRoles(OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
				boolean isAuthor = roles.hasRole(OrganisationRoles.author);
				
				if(hasAdministrativeRoles) {
					params.setHasAdministrator(identityEnv.getIdentity());
				} else if(isAuthor) {
					params.setHasAuthor(identityEnv.getIdentity());
					params.setOnlyAdministrators(Boolean.FALSE);
				} else {
					return new ArrayList<>();
				}
			} else {
				return new ArrayList<>();
			}
			
			List<ExportInfos> exportsList = exportManager.getResultsExport(params);
			List<VFSItem> items = new ArrayList<>(exportsList.size());
			for(ExportInfos export:exportsList) {
				if(export.isNew() || export.isRunning() || export.isCancelled()) {
					continue;
				}
				
				if(export.getExportMetadata() != null
						&& StringHelper.containsNonWhitespace(export.getExportMetadata().getFilePath())) {
					VFSLeaf leaf = VFSManager.olatRootLeaf(export.getExportMetadata().getFilePath());
					items.add(leaf);
				}
			}
			return items;
		} catch (Exception e) {
			log.error("", e);
			return new ArrayList<>();
		}
	}
}
