/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.site;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.CommitteeMembershipsStats;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.RecruitingSecurityCallbackImpl;

/**
 * 
 * Description:<br>
 * The site for staff and committee members
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingSite implements SiteInstance {
	
	private DefaultNavElement curNavElem;
	private NavElement origNavElem;
	
	public RecruitingSite(Locale locale) {
		Translator trans = Util.createPackageTranslator(RecruitingMainController.class, locale);
		origNavElem = new DefaultNavElement(null, trans.translate("topnav.home"), trans.translate("topnav.home.alt"), "o_site_recruiting");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		Roles roles = ureq.getUserSession().getRoles();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Positions", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);
		
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		CommitteeMembershipsStats stats = erFrontendManager.getCommitteeMembershipsStats(ureq.getIdentity());
		RecruitingSecurityCallback secCallback = new RecruitingSecurityCallbackImpl(roles, stats);
		return new RecruitingMainController(ureq, bwControl, secCallback);
	}

	@Override
	public boolean isKeepState() {
		return false;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
