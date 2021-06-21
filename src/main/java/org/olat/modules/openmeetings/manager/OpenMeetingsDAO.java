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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsRoomReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class OpenMeetingsDAO {

	@Autowired
	private DB dbInstance;
	
	private XStream xStream;
	
	@PostConstruct
	public void init() {
		xStream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(xStream);
		xStream.alias("room", OpenMeetingsRoom.class);
		xStream.omitField(OpenMeetingsRoom.class, "property");
		xStream.omitField(OpenMeetingsRoom.class, "numOfUsers");
	}

	
	public OpenMeetingsRoomReference createReference(final BusinessGroup group, final OLATResourceable courseResource, String subIdentifier, OpenMeetingsRoom room) {
		String serialized = serializeRoom(room);
		OpenMeetingsRoomReference ref = new OpenMeetingsRoomReference();
		ref.setLastModified(new Date());
		ref.setRoomId(room.getRoomId());
		ref.setConfig(serialized);
		ref.setGroup(group);
		if(courseResource != null) {
			ref.setResourceTypeName(courseResource.getResourceableTypeName());
			ref.setResourceTypeId(courseResource.getResourceableId());
		}
		ref.setSubIdentifier(subIdentifier);
		dbInstance.getCurrentEntityManager().persist(ref);
		return ref;
	}

	public List<OpenMeetingsRoomReference> getReferences() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref from ").append(OpenMeetingsRoomReference.class.getName()).append(" ref");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), OpenMeetingsRoomReference.class).getResultList();
	}
	
	public OpenMeetingsRoomReference getReference(BusinessGroup group, OLATResourceable courseResource, String subIdentifier) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ref from ").append(OpenMeetingsRoomReference.class.getName()).append(" ref");
		
		boolean where = false;
		if(group != null) {
			where = and(sb, where);
			sb.append(" ref.group.key=:groupKey");
		}
		if(courseResource != null) {
			where = and(sb, where);
			sb.append(" ref.resourceTypeName=:resName and ref.resourceTypeId=:resId");
		}
		if(subIdentifier != null) {
			where = and(sb, where);
			sb.append(" ref.subIdentifier=:subIdentifier");
		}
		
		TypedQuery<OpenMeetingsRoomReference> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), OpenMeetingsRoomReference.class);

		if(group != null) {
			query.setParameter("groupKey", group.getKey());
		}
		if(courseResource != null) {
			query.setParameter("resName", courseResource.getResourceableTypeName());
			query.setParameter("resId", courseResource.getResourceableId());
		}
		if(subIdentifier != null) {
			query.setParameter("subIdentifier", subIdentifier);
		}

		List<OpenMeetingsRoomReference> refs = query.getResultList();
		if(refs.isEmpty()) {
			return null;
		}
		return refs.get(0);
	}
	
	public OpenMeetingsRoomReference updateReference(BusinessGroup group, OLATResourceable courseResource, String subIdentifier, OpenMeetingsRoom room) {
		OpenMeetingsRoomReference property = getReference(group, courseResource, subIdentifier);
		if(property == null) {
			property = createReference(group, courseResource, subIdentifier, room);

		} else {
			String serialized = serializeRoom(room);
			property.setLastModified(new Date());
			property.setConfig(serialized);
			property = dbInstance.getCurrentEntityManager().merge(property);
		}
		return property;
	}
	
	public void delete(OpenMeetingsRoomReference ref) {
		OpenMeetingsRoomReference reloadedRef = dbInstance.getCurrentEntityManager()
				.getReference(OpenMeetingsRoomReference.class, ref.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedRef);
	}
	
	public String serializeRoom(OpenMeetingsRoom room) {
		StringWriter writer = new StringWriter();
		xStream.marshal(room, new CompactWriter(writer));
		writer.flush();
		return writer.toString();
	}
	
	public OpenMeetingsRoom deserializeRoom(String room) {
		return (OpenMeetingsRoom)xStream.fromXML(room);
	}
	
	private final boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}
}