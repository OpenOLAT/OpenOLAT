/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.site;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;

/**
 * 
 * Initial date: 05.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionsContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		return RecruitingSite.class.getName();
	}

	@Override
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
		if(entries != null && !entries.isEmpty()
				&& "Positions".equalsIgnoreCase(entries.get(0).getOLATResourceable().getResourceableTypeName())
				&& entries.get(0).getOLATResourceable().getResourceableId().longValue() > 0) {
			//expand Positions/2344456 in Positions/0/Position/2344456	
		}
		return super.getTabContext(ureq, ores, mainEntry, entries);
	}
}
