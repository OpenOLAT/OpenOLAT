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

import static org.olat.collaboration.CollaborationTools.KEY_CALENDAR_ACCESS;
import static org.olat.collaboration.CollaborationTools.KEY_FOLDER_ACCESS;
import static org.olat.collaboration.CollaborationTools.PROP_CAT_BG_COLLABTOOLS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CollaborationManagerImpl implements CollaborationManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	@Override
	public String getFolderRelPath(OLATResourceable ores) {
		return "/cts/folders/" + ores.getResourceableTypeName() + "/" + ores.getResourceableId();
	}

	@Override
	public Long lookupFolderAccess(OLATResourceable ores) {
		StringBuilder query = new StringBuilder();
		query.append("select prop.longValue from ").append(Property.class.getName()).append(" as prop where ")
		     .append(" prop.category='").append(PROP_CAT_BG_COLLABTOOLS).append("'")
		     .append(" and prop.resourceTypeName='").append(ores.getResourceableTypeName()).append("'")
		     .append(" and prop.resourceTypeId=").append(ores.getResourceableId())
		     .append(" and prop.name='").append(KEY_FOLDER_ACCESS).append("'")
		     .append(" and prop.identity is null and prop.grp is null");
		
		List<Long> props = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.getResultList();
		if(props.isEmpty()) {
			return null;
		}
		return props.get(0);
	}
	
	@Override
	public Map<Long,Long> lookupCalendarAccess(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) {
			return new HashMap<>();
		}
		
		StringBuilder query = new StringBuilder(256);
		query.append("select prop from ").append(Property.class.getName()).append(" as prop where ")
		     .append(" prop.category='").append(PROP_CAT_BG_COLLABTOOLS).append("'")
		     .append(" and prop.resourceTypeName='BusinessGroup'")
		     .append(" and prop.resourceTypeId in (:groupKeys)")
		     .append(" and prop.name='").append(KEY_CALENDAR_ACCESS).append("'")
		     .append(" and prop.identity is null and prop.grp is null");
		
		TypedQuery<Property> dbquery = dbInstance.getCurrentEntityManager().createQuery(query.toString(), Property.class);
		Map<Long,Long> groupKeyToAccess = new HashMap<>();

		int count = 0;
		int batch = 200;
		do {
			int toIndex = Math.min(count + batch, groups.size());
			List<BusinessGroup> toLoad = groups.subList(count, toIndex);
			List<Long> groupKeys = PersistenceHelper.toKeys(toLoad);

			List<Property> props = dbquery.setFirstResult(count)
				.setMaxResults(batch)
				.setParameter("groupKeys", groupKeys)
				.getResultList();
			for(Property prop:props) {
				groupKeyToAccess.put(prop.getResourceTypeId(), prop.getLongValue());
			}
			count += batch;
		} while(count < groups.size());
		return groupKeyToAccess;
	}

	@Override
	public KalendarRenderWrapper getCalendar(BusinessGroup businessGroup, UserRequest ureq, boolean isAdmin) {
		// do not use a global translator since in the fututre a collaborationtools
		// may be shared among users

		// get the calendar
		KalendarRenderWrapper calRenderWrapper = calendarManager.getGroupCalendar(businessGroup);
		boolean isOwner = businessGroupService.hasRoles(ureq.getIdentity(), businessGroup, GroupRoles.coach.name());
		if (!(isAdmin || isOwner)) {
			// check if participants have read/write access
			int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
			Long lCalAccess = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup).lookupCalendarAccess();
			if (lCalAccess != null) iCalAccess = lCalAccess.intValue();
			if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_ALL) {
				calRenderWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			} else {
				calRenderWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			}
		} else {
			calRenderWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		}
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calRenderWrapper.getKalendar(), ureq.getIdentity());
		if (config != null) {
			calRenderWrapper.setConfiguration(config);
		}
		return calRenderWrapper;
	}
}
