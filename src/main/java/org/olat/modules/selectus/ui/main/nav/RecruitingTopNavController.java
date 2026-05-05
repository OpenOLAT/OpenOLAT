/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.main.nav;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.gui.control.OlatTopNavController;
import org.olat.gui.control.OlatTopNavController.Tool;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingTopNavController extends BasicController implements LockableController {
	private static final String ACTION_LOGOUT = "logout";
	
	private final VelocityContainer topNavVC;
	private Link loginLink;
	private List<Disposable> disposableTools = new ArrayList<>();
	
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private UserToolsModule userToolsModule;
	
	public RecruitingTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(OlatTopNavController.class, ureq.getLocale(),
				Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale())));
		
		topNavVC = createVelocityContainer("topnav");
		topNavVC.setDomReplacementWrapperRequired(false);
		
		/* TODO selectus
		if(helpModule.isHelpEnabled() && helpModule.isHelpInHeader()) {
			Component helpLink = helpModule.getHelpProvider()
					.getHelpPageLink(ureq, translate("topnav.help"), translate("topnav.help.alt"), "o_icon o_icon-fw o_icon_help", null, null);
			topNavVC.put("topnav.help", helpLink);
		}
		*/

		if (ureq.getIdentity() == null) {
			topNavVC.contextPut("isGuest", Boolean.TRUE);
			loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
			loginLink.setTooltip("topnav.login.alt");
		} 
		
		loadPersonalTools(ureq);
		
		putInitialPanel(topNavVC);
	}

	@Override
	public void lock() {
		//
	}

	@Override
	public void unlock() {
		//
	}
	
	private void loadPersonalTools(UserRequest ureq) {
		List<Tool> toolSetLinksName = new ArrayList<>();
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		String selectedTools = userToolsModule.getUserTools(prefs);
		if(!StringHelper.containsNonWhitespace(selectedTools)) {
			selectedTools = userToolsModule.getDefaultPresetOfUserTools();
		}
		Set<String> selectedToolSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(selectedTools)) {
			String[] selectedToolArr = selectedTools.split(",");
			for(String selectedTool:selectedToolArr) {
				selectedToolSet.add(UserToolsModule.stripToolKey(selectedTool));
			}
		}
		
		List<UserToolExtension> toolExtensions = userToolsModule.getUserToolExtensions(ureq);
		for (UserToolExtension toolExtension : toolExtensions) {
			// check for sites
			UserTool tool = toolExtension.createUserTool(ureq, getWindowControl(), getLocale());
			if(tool != null) {
				boolean shortCutOnly = toolExtension.isShortCutOnly();
				if(shortCutOnly || selectedToolSet.contains(toolExtension.getUniqueExtensionID())) {
					Component cmp = tool.getMenuComponent(ureq, topNavVC, false);
					String cssId = toolExtension.getShortCutCssId();
					String cssClass = toolExtension.getShortCutCssClass();
					toolSetLinksName.add(new Tool(cssId, cssClass, cmp.getComponentName()));
					disposableTools.add(tool);
				}
			}
		}
		topNavVC.contextPut("toolSet", toolSetLinksName);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		String command = event.getCommand();
		if (source == loginLink) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		} else if (source == topNavVC) {
			if (command.equals(ACTION_LOGOUT)) {
				AuthHelper.doLogout(ureq);
			}
		}
	}
}
