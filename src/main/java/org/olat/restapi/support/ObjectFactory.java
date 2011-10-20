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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.support;

import javax.ws.rs.core.EntityTag;

import org.olat.basesecurity.Authentication;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;

/**
 * Description:<br>
 * Factory for object needed by the REST Api
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ObjectFactory {
	
	public static GroupVO get(BusinessGroup grp) {
		GroupVO vo = new GroupVO();
		vo.setKey(grp.getKey());
		vo.setName(grp.getName());
		vo.setDescription(grp.getDescription());
		vo.setMaxParticipants(grp.getMaxParticipants());
		vo.setMinParticipants(grp.getMinParticipants());
		vo.setType(grp.getType());
		return vo;
	}
	
	public static GroupInfoVO getInformation(BusinessGroup grp) {
		GroupInfoVO vo = new GroupInfoVO();
		vo.setKey(grp.getKey());
		vo.setName(grp.getName());
		vo.setDescription(grp.getDescription());
		vo.setMaxParticipants(grp.getMaxParticipants());
		vo.setMinParticipants(grp.getMinParticipants());
		vo.setType(grp.getType());
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(grp);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			vo.setForumKey(collabTools.getForum().getKey());
		}
		
		String news = collabTools.lookupNews();
		vo.setNews(news);
		
		boolean hasWiki = collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI);
		vo.setHasWiki(hasWiki);
		
		boolean hasFolder = collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER);
		vo.setHasFolder(hasFolder);
		return vo;
	}
	
	public static AuthenticationVO get(Authentication authentication, boolean withCred) {
		AuthenticationVO vo = new AuthenticationVO();
		vo.setKey(authentication.getKey());
		vo.setIdentityKey(authentication.getIdentity().getKey());
		vo.setAuthUsername(authentication.getAuthusername());
		vo.setProvider(authentication.getProvider());
		if(withCred) {
			vo.setCredential(authentication.getCredential());
		}
		return vo;
	}
	
	public static RepositoryEntryVO get(RepositoryEntry entry) {
		RepositoryEntryVO vo = new RepositoryEntryVO();
		vo.setKey(entry.getKey());
		vo.setSoftkey(entry.getSoftkey());
		vo.setResourcename(entry.getResourcename());
		vo.setDisplayname(entry.getDisplayname());
		vo.setResourceableId(entry.getResourceableId());
		vo.setResourceableTypeName(entry.getResourceableTypeName());
		return vo;
	}
	
	public static CourseVO get(ICourse course) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, course.getResourceableId());
		RepositoryEntry	re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
		return get(re, course);
	}
	
	public static CourseVO get(RepositoryEntry re, ICourse course) {
		CourseVO vo = new CourseVO();
		vo.setKey(course.getResourceableId());
		vo.setTitle(course.getCourseTitle());
		vo.setEditorRootNodeId(course.getEditorTreeModel().getRootNode().getIdent());
		vo.setSoftKey(re.getSoftkey());
		vo.setRepoEntryKey(re.getKey());
		return vo;
	}
	
	public static CourseConfigVO getConfig(ICourse course) {
		CourseConfigVO vo = new CourseConfigVO();
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		vo.setSharedFolderSoftKey(config.getSharedFolderSoftkey());
		return vo;
	}
	
	public static CourseNodeVO get(CourseNode node) {
		CourseNodeVO vo = new CourseNodeVO();
		vo.setId(node.getIdent());
		vo.setPosition(node.getPosition());
		vo.setParentId(node.getParent() == null ? null : node.getParent().getIdent());
		
		vo.setShortTitle(node.getShortTitle());
		vo.setShortName(node.getShortName());
		vo.setLongTitle(node.getLongTitle());
		vo.setLearningObjectives(node.getLearningObjectives());

		return vo;
	}
	
	public static EntityTag computeEtag(RepositoryEntry re) {
		int version = re.getVersion();
		Long key = re.getKey();
		return new EntityTag("RepositoryEntry-" + key + "-" + version);
	}
	
	public static ErrorVO get(String pack, String key, String translation) {
		ErrorVO vo = new ErrorVO();
		vo.setCode(pack + ":" + key);
		vo.setTranslation(translation);
		return vo;
	}
	
	public static ErrorVO get(ValidationError error) {
		ErrorVO vo = new ErrorVO();
		vo.setCode("unkown" + ":" + error.getErrorKey());
		vo.setTranslation("Hello");
		return vo;
	}
}
