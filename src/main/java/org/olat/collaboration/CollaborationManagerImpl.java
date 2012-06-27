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

import static org.olat.collaboration.CollaborationTools.KEY_FOLDER_ACCESS;
import static org.olat.collaboration.CollaborationTools.PROP_CAT_BG_COLLABTOOLS;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.properties.Property;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CollaborationManagerImpl extends BasicManager implements CollaborationManager {
	
	
	public String getFolderRelPath(OLATResourceable ores) {
		return "/cts/folders/" + ores.getResourceableTypeName() + "/" + ores.getResourceableId();
	}

	//fxdiff VCRP-8: collaboration tools folder access control
	public Long lookupFolderAccess(OLATResourceable ores) {
		StringBuilder query = new StringBuilder();
		query.append("select prop.longValue from ").append(Property.class.getName()).append(" as prop where ")
		     .append(" prop.category='").append(PROP_CAT_BG_COLLABTOOLS).append("'")
		     .append(" and prop.resourceTypeName='").append(ores.getResourceableTypeName()).append("'")
		     .append(" and prop.resourceTypeId=").append(ores.getResourceableId())
		     .append(" and prop.name='").append(KEY_FOLDER_ACCESS).append("'")
		     .append(" and prop.identity is null and prop.grp is null");
		
		DBQuery dbquery = DBFactory.getInstance().createQuery(query.toString());
		dbquery.setCacheable(true);

		@SuppressWarnings("unchecked")
		List<Long> props = dbquery.list();
		if(props.isEmpty()) {
			return null;
		} else {
			return props.get(0);
		}
	}

	@Override
	public KalendarRenderWrapper getCalendar(BusinessGroup businessGroup, UserRequest ureq, boolean isAdmin) {
	
		// do not use a global translator since in the fututre a collaborationtools
		// may be shared among users

		// get the calendar
		CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
		KalendarRenderWrapper calRenderWrapper = calManager.getGroupCalendar(businessGroup);
		boolean isOwner = BaseSecurityManager.getInstance().isIdentityInSecurityGroup(ureq.getIdentity(), businessGroup.getOwnerGroup());
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
		KalendarConfig config = calManager.findKalendarConfigForIdentity(calRenderWrapper.getKalendar(), ureq);
		if (config != null) {
			calRenderWrapper.getKalendarConfig().setCss(config.getCss());
			calRenderWrapper.getKalendarConfig().setVis(config.isVis());
		}
		calRenderWrapper.getKalendarConfig().setResId(businessGroup.getKey());
		return calRenderWrapper;
	}
	
	
}
