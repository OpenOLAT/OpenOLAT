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
package org.olat.course.nodes.pf.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.SystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.PFCourseNode;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PFUserDataManager implements UserDataExportable {
	
	private static final OLog log = Tracing.createLoggerFor(PFUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PFManager pfManager;

	@Override
	public String getExporterID() {
		return "pf";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		int count = 0;
		File pfArchiveDirectory = new File(archiveDirectory, "ParticipantFolders");
		List<Long> resourceIds = getAlllRepositoryEntryResourceId(identity);
		for(Long resourceId:resourceIds) {
			try {
				ICourse course = CourseFactory.loadCourse(resourceId);
				final List<PFCourseNode> pfNodes = new ArrayList<>();
				new TreeVisitor(node -> {
					if(node instanceof PFCourseNode) {
						pfNodes.add((PFCourseNode)node);
					}
				}, course.getRunStructure().getRootNode(), true).visitAll();
				
				for(PFCourseNode pfNode:pfNodes) {
					exportPFNode(identity, pfNode, course, pfArchiveDirectory);
				}
				
				if(count++ % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("", e);
				dbInstance.rollbackAndCloseSession();
			}
		}
	}
	
	private void exportPFNode(Identity identity, PFCourseNode pfNode, ICourse course, File pfArchiveDirectory) {
		VFSContainer dropBox = pfManager.resolveDropFolder(course.getCourseEnvironment(), pfNode, identity);
		if(dropBox != null) {
			List<VFSItem> droppedItems = dropBox.getItems(new SystemItemFilter());
			if(!droppedItems.isEmpty()) {
				String name = StringHelper.transformDisplayNameToFileSystemName(course.getCourseTitle()) +
						"_" + StringHelper.transformDisplayNameToFileSystemName(getNodeName(pfNode));
				File pfNodeArchiveDirectory = new File(pfArchiveDirectory, name);
				pfNodeArchiveDirectory.mkdirs();
				for(VFSItem droppedItem:droppedItems) {
					FileUtils.copyItemToDir(droppedItem, pfNodeArchiveDirectory, "Copy participant folder's file");
				}
			}
		}
	}
	
	private String getNodeName(PFCourseNode pfNode) {
		try {
			if(StringHelper.containsNonWhitespace(pfNode.getShortTitle())) {
				return pfNode.getShortTitle();
			}
			if(StringHelper.containsNonWhitespace(pfNode.getLongTitle())) {
				return pfNode.getLongTitle();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return pfNode.getIdent();
	}
	
	private List<Long> getAlllRepositoryEntryResourceId(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select res.resId from repositoryentry as v")
		  .append(" inner join v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where res.resName='CourseModule' and membership.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
}
