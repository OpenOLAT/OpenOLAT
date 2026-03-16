/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.site;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  17 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		return ApplicationSite.class.getName();
	}
}
