/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.site;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

import org.olat.modules.selectus.dispatcher.AbstractRecruitingDispatcher;
import org.olat.modules.selectus.dispatcher.PositionDispatcher;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(ureq.getUserSession().getRoles().isGuestOnly()
				&& PositionDispatcher.POSITION_SOURCE.equals(ureq.getUserSession().getEntry(AbstractRecruitingDispatcher.DISPATCHER_SOURCE))) {
			Locale locale = ureq.getLocale();
			return new ApplicationSite(locale);
		}
		return null;
	}
}
