/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import java.util.List;

/**
 * Description:<br>
 * TODO: bja Class Description for ChecklistFilter
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistFilter {
	
	private String title;
	private List<Long> identityIds;
	
	public ChecklistFilter(String title, List<Long> identityIds) {
		this.title = title;
		this.identityIds = identityIds;
	}
}
