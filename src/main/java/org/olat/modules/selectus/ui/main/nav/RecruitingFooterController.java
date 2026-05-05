/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.main.nav;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.UserLoggedInCounter;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.gui.control.OlatFooterController;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  19 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingFooterController extends BasicController implements LockableController {
	
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public RecruitingFooterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(OlatFooterController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(BaseFullWebappController.class, getLocale(), getTranslator()));
		
		VelocityContainer vc = createVelocityContainer("footer");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		
		String url = recruitingModule.getAboutUrl();
		ExternalLink aboutLink = LinkFactory.createExternalLink("about", "about", url);
		aboutLink.setName(Settings.getApplicationName());
		aboutLink.setTarget("_blank");
		vc.put("about", aboutLink);
		
		String copyright = translate("footer.copyright", new String[]{Integer.toString(cal.get(Calendar.YEAR))});
		vc.contextPut("copyright", copyright);
		vc.contextPut("now", DateCellRenderer.format(cal.getTime()));
		
		// Push brasato framework number to velocity
		vc.contextPut("appName", Settings.getApplicationName());
		vc.contextPut("version", Settings.getVersion()); 
		vc.contextPut("buildInfos", " (Build " + Settings.getBuildIdentifier()  + ")");
		
		String disclaimerUrl = recruitingModule.getDisclaimerUrl();
		if(StringHelper.containsNonWhitespace(disclaimerUrl)) {
			vc.contextPut("disclaimerUrl", disclaimerUrl);
		}
		
		String impressumUrl = recruitingModule.getImpressumUrl();
		if(StringHelper.containsNonWhitespace(impressumUrl)) {
			vc.contextPut("impressumUrl", impressumUrl);
		}
		/*
		if(helpModule.isHelpEnabled() && helpModule.isHelpInFooter()) {
			Component helpLink = helpModule.getHelpProvider()
				.getHelpPageLink(ureq, translate("topnav.help"), translate("topnav.help.alt"), "o_icon o_icon-fw o_icon_help", null, null);
			vc.put("topnav.help", helpLink);
		}
		*/
		
		if(ureq.getUserSession().isAuthenticated() && !ureq.getUserSession().getRoles().isGuestOnly()) {
			String username = ureq.getIdentity().getName();
			String translated = translate("footer.logged.in.as", new String[]{username});
			vc.contextPut("username", translated);
		}
		
		// Show user count
		vc.put("userCounter", new UserLoggedInCounter());
		
		putInitialPanel(vc);
	}
	
	@Override
	public void lock() {
		//
	}

	@Override
	public void unlock() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
