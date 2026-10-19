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

import java.util.List;

import org.olat.admin.restapi.ApiAuditLogAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.restapi.audit.ApiAuditChannel;

/**
 * The administration of the MCP server with four tabs: configuration,
 * profiles, tokens and the audit log of the channel mcp.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAdminMainController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink;
	private final Link profilesLink;
	private final Link tokensLink;
	private final Link auditLogLink;

	private McpConfigurationController configurationCtrl;
	private McpProfileListController profilesCtrl;
	private McpTokenAdminController tokensCtrl;
	private ApiAuditLogAdminController auditLogCtrl;

	public McpAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("mcp_admin");
		putInitialPanel(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("segment.configuration", mainVC, this);
		configurationLink.setElementCssClass("o_sel_mcp_configuration");
		segmentView.addSegment(configurationLink, true);
		profilesLink = LinkFactory.createLink("segment.profiles", mainVC, this);
		profilesLink.setElementCssClass("o_sel_mcp_profiles");
		segmentView.addSegment(profilesLink, false);
		tokensLink = LinkFactory.createLink("segment.tokens", mainVC, this);
		tokensLink.setElementCssClass("o_sel_mcp_tokens");
		segmentView.addSegment(tokensLink, false);
		auditLogLink = LinkFactory.createLink("segment.auditlog", mainVC, this);
		auditLogLink.setElementCssClass("o_sel_mcp_auditlog");
		segmentView.addSegment(auditLogLink, false);

		doOpenConfiguration(ureq);
	}

	/**
	 * Opens the segment named by the first entry of the business path:
	 * Configuration, Profiles, Tokens or AuditLog. The remaining entries go to
	 * the segment controller when it can activate them.
	 */
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		Controller ctrl;
		if("Profiles".equalsIgnoreCase(type)) {
			doOpenProfiles(ureq);
			segmentView.select(profilesLink);
			ctrl = profilesCtrl;
		} else if("Tokens".equalsIgnoreCase(type)) {
			doOpenTokens(ureq);
			segmentView.select(tokensLink);
			ctrl = tokensCtrl;
		} else if("AuditLog".equalsIgnoreCase(type)) {
			doOpenAuditLog(ureq);
			segmentView.select(auditLogLink);
			ctrl = auditLogCtrl;
		} else if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
			ctrl = configurationCtrl;
		} else {
			return;
		}
		if(ctrl instanceof Activateable2 activateable && entries.size() > 1) {
			activateable.activate(ureq, entries.subList(1, entries.size()), state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent sve) {
			Component clickedLink = mainVC.getComponent(sve.getComponentName());
			if(clickedLink == configurationLink) {
				doOpenConfiguration(ureq);
			} else if(clickedLink == profilesLink) {
				doOpenProfiles(ureq);
			} else if(clickedLink == tokensLink) {
				doOpenTokens(ureq);
			} else if(clickedLink == auditLogLink) {
				doOpenAuditLog(ureq);
			}
		}
	}

	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new McpConfigurationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		show(ureq, configurationCtrl);
	}

	private void doOpenProfiles(UserRequest ureq) {
		if(profilesCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Profiles"), null);
			profilesCtrl = new McpProfileListController(ureq, bwControl);
			listenTo(profilesCtrl);
		} else {
			// the member counts may have changed in the meantime
			profilesCtrl.reloadModel();
		}
		show(ureq, profilesCtrl);
	}

	private void doOpenTokens(UserRequest ureq) {
		if(tokensCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Tokens"), null);
			tokensCtrl = new McpTokenAdminController(ureq, bwControl);
			listenTo(tokensCtrl);
		} else {
			tokensCtrl.reloadModel();
		}
		show(ureq, tokensCtrl);
	}

	private void doOpenAuditLog(UserRequest ureq) {
		if(auditLogCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("AuditLog"), null);
			auditLogCtrl = new ApiAuditLogAdminController(ureq, bwControl, ApiAuditChannel.mcp);
			listenTo(auditLogCtrl);
		}
		show(ureq, auditLogCtrl);
	}

	private void show(UserRequest ureq, Controller ctrl) {
		addToHistory(ureq, ctrl);
		mainVC.put("segmentCmp", ctrl.getInitialComponent());
	}
}
