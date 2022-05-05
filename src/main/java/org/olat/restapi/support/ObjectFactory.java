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
package org.olat.restapi.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.EntityTag;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;

/**
 * Description:<br>
 * Factory for object needed by the REST Api
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ObjectFactory {
	private static final Logger log = Tracing.createLoggerFor(ObjectFactory.class);
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	public static String formatDate(Date date) {
		if(date == null) return null;
		synchronized(format) {
			return format.format(date);
		}
	}
	
	public static Date parseDate(String date) {
		if(!StringHelper.containsNonWhitespace(date)) {
			return null;
		}
		
		try {
			synchronized(format) {
				return format.parse(date);
			}
		} catch (ParseException e) {
			log.warn("", e);
			return null;
		}
	}
	
	public static GroupInfoVO getInformation(Identity identity, BusinessGroup grp) {
		GroupInfoVO vo = new GroupInfoVO();
		vo.setKey(grp.getKey());
		vo.setName(grp.getName());
		vo.setDescription(grp.getDescription());
		
		
		vo.setMaxParticipants(grp.getMaxParticipants());
		vo.setMinParticipants(grp.getMinParticipants());
		vo.setType("LearningGroup");
		
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
		boolean hasFolderWrite = hasFolder;
		if(hasFolder) {
			Long access = collabTools.lookupFolderAccess();
			if(access != null && access.intValue() == CollaborationTools.FOLDER_ACCESS_OWNERS) {
				//is owner?
				hasFolderWrite = CoreSpringFactory.getImpl(BusinessGroupService.class).hasRoles(identity, grp, GroupRoles.coach.name());
			}
		}
		vo.setFolderWrite(hasFolderWrite);
		
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
	
	public static CourseVO get(ICourse course) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, course.getResourceableId());
		RepositoryEntry	re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
		return get(re, course);
	}
	
	public static CourseVO get(RepositoryEntry re, ICourse course) {
		CourseVO vo = new CourseVO();
		vo.setKey(course.getResourceableId());
		vo.setDisplayName(re.getDisplayname());
		vo.setDescription(re.getDescription());
		vo.setTitle(course.getCourseTitle());
		vo.setEditorRootNodeId(course.getEditorTreeModel().getRootNode().getIdent());
		vo.setNodeAccessType(course.getCourseConfig().getNodeAccessType().getType());
		vo.setSoftKey(re.getSoftkey());
		vo.setRepoEntryKey(re.getKey());
		OLATResource resource = re.getOlatResource();
		if(resource != null) {
			vo.setOlatResourceKey(resource.getKey());
			vo.setOlatResourceId(resource.getResourceableId());
			vo.setOlatResourceTypeName(resource.getResourceableTypeName());
		}
		vo.setAuthors(re.getAuthors());
		vo.setLocation(re.getLocation());
		vo.setExternalId(re.getExternalId());
		vo.setExternalRef(re.getExternalRef());
		vo.setManagedFlags(re.getManagedFlagsString());
		if(re.getLifecycle() != null) {
			vo.setLifecycle(new RepositoryEntryLifecycleVO(re.getLifecycle()));
		}
		return vo;
	}
	
	public static CourseConfigVO getConfig(ICourse course) {
		CourseConfigVO vo = new CourseConfigVO();
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		vo.setCalendar(Boolean.valueOf(config.isCalendarEnabled()));
		vo.setChat(Boolean.valueOf(config.isChatEnabled()));
		vo.setCssLayoutRef(config.getCssLayoutRef());
		vo.setEfficencyStatement(Boolean.valueOf(config.isEfficencyStatementEnabled()));
		vo.setGlossarySoftkey(config.getGlossarySoftKey());
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
		vo.setDescription(node.getDescription());
		vo.setObjectives(node.getObjectives());
		vo.setInstruction(node.getInstruction());
		vo.setInstructionalDesign(node.getInstructionalDesign());

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
