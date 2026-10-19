/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Answers "which tools does user X get?": a user search, then the result of
 * the grant resolution for the chosen identity with its matching profiles,
 * the tools grouped by pack, the effective request limit and the number of
 * its tokens. Read only.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAccessCheckController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link otherUserButton;
	private final Link closeButton;

	private UserSearchFlexiController userSearchCtrl;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private McpToolRegistry toolRegistry;

	public McpAccessCheckController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("access_check");
		otherUserButton = LinkFactory.createButton("access.check.other", mainVC, this);
		otherUserButton.setElementCssClass("o_sel_mcp_access_check_other");
		closeButton = LinkFactory.createButton("close", mainVC, this);
		putInitialPanel(mainVC);
		doOpenSearch(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == otherUserButton) {
			doOpenSearch(ureq);
		} else if(source == closeButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == userSearchCtrl && event instanceof SingleIdentityChosenEvent sice) {
			doShowResult(sice.getChosenIdentity());
		}
		super.event(ureq, source, event);
	}

	private void doOpenSearch(UserRequest ureq) {
		removeAsListenerAndDispose(userSearchCtrl);
		userSearchCtrl = new UserSearchFlexiController(ureq, getWindowControl(), (GroupRoles)null, false);
		listenTo(userSearchCtrl);
		mainVC.put("search", userSearchCtrl.getInitialComponent());
		mainVC.contextPut("showResult", Boolean.FALSE);
	}

	private void doShowResult(Identity identity) {
		mainVC.remove("search");
		removeAsListenerAndDispose(userSearchCtrl);
		userSearchCtrl = null;

		McpGrants grants = mcpService.resolveGrants(identity);

		mainVC.contextPut("showResult", Boolean.TRUE);
		mainVC.contextPut("userName", StringHelper.escapeHtml(userManager.getUserDisplayName(identity.getUser())));
		mainVC.contextPut("hasAccess", Boolean.valueOf(!grants.isEmpty()));
		mainVC.contextPut("numOfTokens", Integer.toString(tokenService.countTokens(identity)));
		mainVC.contextPut("moduleDisabled", Boolean.valueOf(!mcpModule.isEnabled()));
		if(grants.isEmpty()) {
			return;
		}

		mainVC.contextPut("profiles", grants.profileIdentifiers());
		Integer override = grants.requestsPerMinute();
		int rpm = override == null ? mcpModule.getRateLimitRequestsPerMinute() : override.intValue();
		String rpmKey = override == null ? "access.check.rpm.default" : "access.check.rpm.override";
		mainVC.contextPut("requestsPerMinute", translate(rpmKey, Integer.toString(rpm)));

		Map<String,List<String>> toolsByPack = new TreeMap<>();
		TreeSet<String> unknownTools = new TreeSet<>();
		for(String toolName:grants.tools()) {
			McpToolProvider provider = toolRegistry.getProvider(toolName);
			if(provider == null) {
				unknownTools.add(toolName);
			} else {
				String title = provider.getDefinition().title();
				if(!StringHelper.containsNonWhitespace(title)) {
					title = toolName;
				}
				String key = provider.getScope().isWrite() ? "profile.tool.label.write" : "profile.tool.label";
				toolsByPack.computeIfAbsent(provider.getPack(), pack -> new ArrayList<>())
					.add(translate(key, title, toolName));
			}
		}

		List<PackTools> packs = new ArrayList<>(toolsByPack.size());
		for(Map.Entry<String,List<String>> entry:toolsByPack.entrySet()) {
			List<String> tools = entry.getValue();
			tools.sort(String::compareTo);
			packs.add(new PackTools(translate("pack." + entry.getKey()), tools));
		}
		mainVC.contextPut("packs", packs);
		mainVC.contextPut("unknownTools", new ArrayList<>(unknownTools));
	}

	/**
	 * The granted tools of one pack, labels already translated.
	 */
	public static class PackTools {

		private final String name;
		private final List<String> tools;

		public PackTools(String name, List<String> tools) {
			this.name = name;
			this.tools = tools;
		}

		public String getName() {
			return name;
		}

		public List<String> getTools() {
			return tools;
		}
	}
}
