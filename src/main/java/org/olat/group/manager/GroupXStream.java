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
package org.olat.group.manager;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Description:<br>
 * Complex XStream mapping to remove dependency to edenlib 
 * 
 * <P>
 * Initial Date:  5 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupXStream {
	
	private final XStream xstream;
	
	public GroupXStream() {
		xstream = XStreamHelper.createXStreamInstance();
		Class<?>[] types = new Class[] {
				CollabTools.class, Group.class, Area.class, AreaCollection.class, GroupCollection.class,
				OLATGroupExport.class, ArrayList.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.alias("OLATGroupExport", OLATGroupExport.class);
		xstream.alias("AreaCollection", AreaCollection.class);
		xstream.alias("GroupCollection", GroupCollection.class);

		xstream.addImplicitCollection(AreaCollection.class, "groups", "Area", Area.class);
		xstream.addImplicitCollection(GroupCollection.class, "groups", "Group", Group.class);
		
		xstream.aliasAttribute(OLATGroupExport.class, "areas", "AreaCollection");
		xstream.aliasAttribute(OLATGroupExport.class, "groups", "GroupCollection");

		xstream.alias("Area", Area.class);
		xstream.aliasAttribute(Area.class, "name", "name");
		xstream.aliasAttribute(Area.class, "key", "key");
		xstream.addImplicitCollection(Area.class, "description", "Description", String.class);
		xstream.aliasAttribute(Area.class, "description", "description");

		xstream.alias("Group", Group.class);
		xstream.alias("CollabTools", CollabTools.class);
		xstream.addImplicitCollection(Group.class, "areaRelations", "AreaRelation", String.class);
		xstream.addImplicitCollection(Group.class, "description", "Description", String.class);
		xstream.aliasAttribute(Group.class, "key", "key");
		xstream.aliasAttribute(Group.class, "name", "name");
		xstream.aliasAttribute(Group.class, "maxParticipants", "maxParticipants");
		xstream.aliasAttribute(Group.class, "minParticipants", "minParticipants");
		xstream.aliasAttribute(Group.class, "waitingList", "waitingList");
		xstream.aliasAttribute(Group.class, "autoCloseRanks", "autoCloseRanks");
		xstream.aliasAttribute(Group.class, "showOwners", "showOwners");
		xstream.aliasAttribute(Group.class, "showParticipants", "showParticipants");
		xstream.aliasAttribute(Group.class, "showWaitingList", "showWaitingList");
		xstream.aliasAttribute(Group.class, "description", "description");
		xstream.aliasAttribute(Group.class, "info", "info");
		xstream.aliasAttribute(Group.class, "folderAccess", "folderAccess");
		
		//CollabTools
		xstream.aliasAttribute(Group.class, "tools", "CollabTools");
		xstream.aliasAttribute(CollabTools.class, "hasNews", "hasNews");
		xstream.aliasAttribute(CollabTools.class, "hasContactForm", "hasContactForm");
		xstream.aliasAttribute(CollabTools.class, "hasCalendar", "hasCalendar");
		xstream.aliasAttribute(CollabTools.class, "hasFolder", "hasFolder");
		xstream.aliasAttribute(CollabTools.class, "hasForum", "hasForum");
		xstream.aliasAttribute(CollabTools.class, "hasChat", "hasChat");
		xstream.aliasAttribute(CollabTools.class, "hasWiki", "hasWiki");
		xstream.aliasAttribute(CollabTools.class, "hasPortfolio", "hasPortfolio");
	}
	
	public OLATGroupExport fromXML(InputStream input) {
		return (OLATGroupExport)xstream.fromXML(input);
	}
	
	public OLATGroupExport fromXML(File input) {
		return (OLATGroupExport)xstream.fromXML(input);
	}
	
	public String toXML(Object input) {
		return xstream.toXML(input);
	}
	
	public void toXML(Object input, OutputStream out) {
		xstream.toXML(input, out);
	}
}

class OLATGroupExport {
	private AreaCollection areas;
	private GroupCollection groups;
	
	public AreaCollection getAreas() {
		return areas;
	}
	
	public void setAreas(AreaCollection areas) {
		this.areas = areas;
	}
	
	public GroupCollection getGroups() {
		return groups;
	}
	
	public void setGroups(GroupCollection groups) {
		this.groups = groups;
	}
}

class AreaCollection {
	private List<Area> groups = new ArrayList<>();

	public List<Area> getGroups() {
		return groups;
	}

	public void setGroups(List<Area> groups) {
		this.groups = groups;
	}
}

class GroupCollection {
	private List<Group> groups = new ArrayList<>();

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
}

class Area {
	private Long key;
	private String name;
	private List<String> description;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getDescription() {
		return description;
	}
	
	public void setDescription(List<String> description) {
		this.description = description;
	}
}

class Group {
	private Long key;
	private String name;
	private Integer minParticipants;
	private Integer maxParticipants;
	private Boolean waitingList;
	private Boolean autoCloseRanks;
	private Boolean showOwners;
	private Boolean showParticipants;
	private Boolean showWaitingList;
	private List<String> description;
	private CollabTools tools;
	private List<String> areaRelations;
	private Long calendarAccess;
	private String info;
	private Long folderAccess;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getMinParticipants() {
		return minParticipants;
	}
	
	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}
	
	public Integer getMaxParticipants() {
		return maxParticipants;
	}
	
	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}
	
	public Boolean getWaitingList() {
		return waitingList;
	}
	
	public void setWaitingList(Boolean waitingList) {
		this.waitingList = waitingList;
	}
	
	public Boolean getAutoCloseRanks() {
		return autoCloseRanks;
	}
	
	public void setAutoCloseRanks(Boolean autoCloseRanks) {
		this.autoCloseRanks = autoCloseRanks;
	}
	
	public Boolean getShowOwners() {
		return showOwners;
	}
	
	public void setShowOwners(Boolean showOwners) {
		this.showOwners = showOwners;
	}
	
	public Boolean getShowParticipants() {
		return showParticipants;
	}
	
	public void setShowParticipants(Boolean showParticipants) {
		this.showParticipants = showParticipants;
	}
	
	public Boolean getShowWaitingList() {
		return showWaitingList;
	}
	
	public void setShowWaitingList(Boolean showWaitingList) {
		this.showWaitingList = showWaitingList;
	}
	
	public List<String> getDescription() {
		return description;
	}
	
	public void setDescription(List<String> description) {
		this.description = description;
	}
	
	public CollabTools getTools() {
		return tools;
	}
	
	public void setTools(CollabTools tools) {
		this.tools = tools;
	}
	
	public List<String> getAreaRelations() {
		return areaRelations;
	}
	
	public void setAreaRelations(List<String> areaRelations) {
		this.areaRelations = areaRelations;
	}
	
	public Long getCalendarAccess() {
		return calendarAccess;
	}
	
	public void setCalendarAccess(Long calendarAccess) {
		this.calendarAccess = calendarAccess;
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public Long getFolderAccess() {
		return folderAccess;
	}
	
	public void setFolderAccess(Long folderAccess) {
		this.folderAccess = folderAccess;
	}
}

class CollabTools {
	private boolean hasNews;
	private boolean hasContactForm;
	private boolean hasCalendar;
	private boolean hasFolder;
	private boolean hasForum;
	private boolean hasChat;
	private boolean hasWiki;
	private boolean hasPortfolio;
	
	public boolean isHasNews() {
		return hasNews;
	}
	
	public void setHasNews(boolean hasNews) {
		this.hasNews = hasNews;
	}
	
	public boolean isHasContactForm() {
		return hasContactForm;
	}
	
	public void setHasContactForm(boolean hasContactForm) {
		this.hasContactForm = hasContactForm;
	}
	
	public boolean isHasCalendar() {
		return hasCalendar;
	}
	
	public void setHasCalendar(boolean hasCalendar) {
		this.hasCalendar = hasCalendar;
	}
	
	public boolean isHasFolder() {
		return hasFolder;
	}
	
	public void setHasFolder(boolean hasFolder) {
		this.hasFolder = hasFolder;
	}
	
	public boolean isHasForum() {
		return hasForum;
	}
	
	public void setHasForum(boolean hasForum) {
		this.hasForum = hasForum;
	}
	
	public boolean isHasChat() {
		return hasChat;
	}
	
	public void setHasChat(boolean hasChat) {
		this.hasChat = hasChat;
	}
	
	public boolean isHasWiki() {
		return hasWiki;
	}
	
	public void setHasWiki(boolean hasWiki) {
		this.hasWiki = hasWiki;
	}
	
	public boolean isHasPortfolio() {
		return hasPortfolio;
	}
	
	public void setHasPortfolio(boolean hasPortfolio) {
		this.hasPortfolio = hasPortfolio;
	}
}