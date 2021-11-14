/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.login.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.LoginModule;

/**
 * 
 * Initial date: 14 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasswordAdminController extends BasicController implements Activateable2 {

	private static final String SYNTAX_RES_TYPE = "syntax";
	private static final String POLICY_RES_TYPE = "policy";
	
	private VelocityContainer mainVC;
	private final Link syntaxLink;
	private final Link policyLink;
	private final SegmentViewComponent segmentView;
	
	private PasswordSyntaxController syntaxCtrl;
	private PasswordPolicyController policyCtrl;
	
	public PasswordAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		syntaxLink = LinkFactory.createLink("admin.password.syntax", mainVC, this);
		segmentView.addSegment(syntaxLink, true);
		policyLink = LinkFactory.createLink("admin.password.policy", mainVC, this);
		segmentView.addSegment(policyLink, false);

		doOpenSyntax(ureq);
		putInitialPanel(mainVC);
	}
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SYNTAX_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenSyntax(ureq);
			segmentView.select(syntaxLink);
		} else if(POLICY_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenPolicy(ureq);
			segmentView.select(policyLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == syntaxLink) {
					doOpenSyntax(ureq);
				} else if (clickedLink == policyLink) {
					doOpenPolicy(ureq);
				}
			}
		}
	}
	
	private void doOpenSyntax(UserRequest ureq) {
		if(syntaxCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(SYNTAX_RES_TYPE), null);
			syntaxCtrl = new PasswordSyntaxController(ureq, swControl);
			listenTo(syntaxCtrl);
		} else {
			addToHistory(ureq, syntaxCtrl);
		}
		mainVC.put("segmentCmp", syntaxCtrl.getInitialComponent());
	}
	
	private void doOpenPolicy(UserRequest ureq) {
		if(policyCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(POLICY_RES_TYPE), null);
			policyCtrl = new PasswordPolicyController(ureq, swControl);
			listenTo(policyCtrl);
		} else {
			addToHistory(ureq, policyCtrl);
		}
		mainVC.put("segmentCmp", policyCtrl.getInitialComponent());
	}
}
