package org.olat.gui.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserToolsMenuController extends BasicController  {

	private static final String ACTION_LOGOUT = "logout";
	
	private final VelocityContainer menuVC;
	private List<Disposable> disposableTools = new ArrayList<>();
	
	@Autowired
	private UserToolsModule userToolsModule;
	
	public UserToolsMenuController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		menuVC = createVelocityContainer("menu");
		menuVC.setDomReplacementWrapperRequired(false);
		if(ureq.getIdentity() != null && ureq.getUserSession() != null && ureq.getUserSession().getRoles() != null) {
			boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
			boolean isInvitee = ureq.getUserSession().getRoles().isInvitee();
			if(!isGuest && !isInvitee) {
				loadPersonalTools(ureq);
			}
		}
		putInitialPanel(menuVC);
	}

	private void loadPersonalTools(UserRequest ureq) {
		List<String> linksName = new ArrayList<String>();
		List<String> configLinksName = new ArrayList<String>();
		List<String> searchLinksName = new ArrayList<String>();
		List<String> systemLinksName = new ArrayList<String>();
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		String selectedTools = (String)prefs.get(WindowManager.class, "user-tools");
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
				UserToolCategory category = toolExtension.getUserToolCategory();
				boolean shortCutOnly = toolExtension.isShortCutOnly();
				if(!shortCutOnly && !selectedToolSet.contains(toolExtension.getUniqueExtensionID())) {
					Component link = tool.getMenuComponent(ureq, menuVC);
					String linkName = link.getComponentName();
					switch(category) {
						case search: searchLinksName.add(linkName); break;
						case personal: linksName.add(linkName); break;
						case config: configLinksName.add(linkName); break;
						case system: systemLinksName.add(linkName); break;
					}
					disposableTools.add(tool);
				}
			}
		}
		
		menuVC.contextPut("personalTools", linksName);
		menuVC.contextPut("configs", configLinksName);
		menuVC.contextPut("systems", systemLinksName);
		menuVC.contextPut("searchs", searchLinksName);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuVC) {
			String command = event.getCommand();
			if (command.equals(ACTION_LOGOUT)) {
				AuthHelper.doLogout(ureq);
			}
		}
	}

	@Override
	protected void doDispose() {
		for(Disposable disposableTool:disposableTools) {
			disposableTool.dispose();
		}
	}
}
