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
	private List<Area> groups = new ArrayList<Area>();

	public List<Area> getGroups() {
		return groups;
	}

	public void setGroups(List<Area> groups) {
		this.groups = groups;
	}
}

class GroupCollection {
	private List<Group> groups = new ArrayList<Group>();

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
}

class Area {
	public Long key;
	public String name;
	public List<String> description;
}

class Group {
	public Long key;
	public String name;
	public Integer minParticipants;
	public Integer maxParticipants;
	public Boolean waitingList;
	public Boolean autoCloseRanks;
	public Boolean showOwners;
	public Boolean showParticipants;
	public Boolean showWaitingList;
	public List<String> description;
	public CollabTools tools;
	public List<String> areaRelations;
	public Long calendarAccess;
	public String info;
	public Long folderAccess;
}

class CollabTools {
	public boolean hasNews;
	public boolean hasContactForm;
	public boolean hasCalendar;
	public boolean hasFolder;
	public boolean hasForum;
	public boolean hasChat;
	public boolean hasWiki;
	public boolean hasPortfolio;
}