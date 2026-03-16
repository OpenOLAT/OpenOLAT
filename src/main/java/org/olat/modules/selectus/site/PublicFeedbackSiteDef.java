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
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.dispatcher.AbstractRecruitingDispatcher;
import org.olat.modules.selectus.dispatcher.PublicFeedbackDispatcher;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if((PublicFeedbackDispatcher.PUBLIC_FEEDBACK_SOURCE.equals(ureq.getUserSession().getEntry(AbstractRecruitingDispatcher.DISPATCHER_SOURCE)))
				|| (StringHelper.containsNonWhitespace((String)ureq.getUserSession().getEntry(PublicFeedbackDispatcher.PUBLIC_FEEDBACK_ID)))) {
			Locale locale = ureq.getLocale();
			return new PublicFeedbackSite(locale);
		}
		return null;
	}
}
