/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.cns.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.Overview;
import org.olat.course.nodes.st.OverviewController;
import org.olat.course.nodes.st.OverviewFactory;
import org.olat.course.run.scoring.AssessmentAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.course.style.ui.HeaderContentController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSSelectionDetailController extends BasicController {
	
	public static final Event SELECT_EVENT = new Event("cns-select");
	
	private Link selectLink;
	
	private final CNSSelectionRow row;
	
	@Autowired
	private LearningPathService learningPathService;

	protected CNSSelectionDetailController(UserRequest ureq, WindowControl wControl, OverviewFactory overviewFactory,
			CNSSelectionRow row, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(HeaderContentController.class, getLocale(), getTranslator()));
		this.row = row;
		
		VelocityContainer mainVC = createVelocityContainer("node_details");
		putInitialPanel(mainVC);
		
		org.olat.course.nodes.st.Overview.Builder overviewBuilder = Overview.builder();
		overviewBuilder.withGoToNodeLinkEnabled(false);
		overviewFactory.appendCourseNodeInfos(overviewBuilder, row.getCourseNode());
		overviewFactory.appendCourseStyleInfos(overviewBuilder, row.getCourseNode());
		if (row.isSelected() && userCourseEnv.getScoreAccounting() instanceof AssessmentAccounting assessmentAccounting) {
			overviewFactory.appendScoreAccountingInfos(overviewBuilder, row.getCourseNode(), assessmentAccounting);
		} else {
			overviewFactory.appendLearningPathConfigs(overviewBuilder, row.getCourseNode(), learningPathService);
		}
		overviewBuilder.withLearningPathStatus(row.getLearningPathStatus());
		Overview overview = overviewBuilder.build();
		
		List<Link> links = null;
		if (!userCourseEnv.isCourseReadOnly() && row.getSelectLink() != null) {
			selectLink = LinkFactory.createCustomLink("o_cns_select_" + row.getCourseNode().getIdent(), "select",
					null, Link.BUTTON + Link.NONTRANSLATED, null, this);
			selectLink.setCustomDisplayText(translate("select"));
			selectLink.setPrimary(true);
			selectLink.setUserObject(row);
			links = List.of(selectLink);
		}
		
		OverviewController overviewCtrl = new OverviewController(ureq, getWindowControl(), overview, null, links);
		listenTo(overviewCtrl);
		mainVC.put("header", overviewCtrl.getInitialComponent());
		
		Builder builder = Header.builder();
		CourseStyleUIFactory.addMetadata(builder, row.getCourseNode(), CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT, false);
		Header header = builder.build();
		mainVC.contextPut("item", header);
	}
	
	public CNSSelectionRow getRow() {
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == selectLink) {
			fireEvent(ureq, SELECT_EVENT);
		}
	}

}
