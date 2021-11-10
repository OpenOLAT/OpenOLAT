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
package org.olat.course.nodes.st;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.learningpath.ui.LearningPathListController;
import org.olat.course.style.TeaserImageStyle;
import org.olat.modules.assessment.ui.AssessmentForm;

/**
 * 
 * Initial date: 15 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewController extends BasicController {

	private final Link nodeLink;
	private final Controller peekViewCtrl;
	private final DueDateConfigFormatter dueDateConfigFormatter;

	public OverviewController(UserRequest ureq, WindowControl wControl, Overview overview, Controller peekViewCtrl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentForm.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathListController.class, getLocale(), getTranslator()));
		this.peekViewCtrl = peekViewCtrl;
		this.dueDateConfigFormatter = DueDateConfigFormatter.create(getLocale());
		
		VelocityContainer mainVC = createVelocityContainer("overview");
		
		mainVC.contextPut("item", overview);
		
		if (overview.getTeaserImageMapper() != null) {
			String mapperID = overview.getTeaserImageID() != null
					? overview.getTeaserImageID()
					: "teaserimage-" + CodeHelper.getRAMUniqueID();
			String teaserImageUrl = registerCacheableMapper(ureq, mapperID, overview.getTeaserImageMapper());
			mainVC.contextPut("teaserImageUrl", teaserImageUrl);
			mainVC.contextPut("cover", TeaserImageStyle.cover == overview.getTeaserImageStyle());
			mainVC.contextPut("gradient", TeaserImageStyle.gradient == overview.getTeaserImageStyle());
		}
		
		nodeLink = LinkFactory.createLink("nodeLink", mainVC, this);
		nodeLink.setCustomDisplayText(StringHelper.escapeHtml(overview.getTitle()));
		nodeLink.setIconLeftCSS("o_icon o_icon-fw " + overview.getIconCss());
		nodeLink.setUserObject(overview.getNodeIdent());
		nodeLink.setElementCssClass("o_gotoNode");
		nodeLink.setEnabled(overview.getNoAccessMessage() == null);
		
		mainVC.contextPut("assessmentInfos", getAssessmentInfos(overview));
		
		if (peekViewCtrl != null) {
			mainVC.put("peekView", this.peekViewCtrl.getInitialComponent());
			listenTo(peekViewCtrl);
		}
		
		putInitialPanel(mainVC);
	}

	private String getAssessmentInfos(Overview overview) {
		boolean separator = false;
		StringBuilder sb = new StringBuilder();
		String formattedStartDate = dueDateConfigFormatter.formatDueDateConfig(overview.getStartDateConfig());
		if (StringHelper.containsNonWhitespace(formattedStartDate)) {
			sb.append(translate("table.header.start")).append(": ").append(formattedStartDate);
			separator = true;
		}
		String formattedEndDate = dueDateConfigFormatter.formatDueDateConfig(overview.getEndDateConfig());
		if (StringHelper.containsNonWhitespace(formattedEndDate)) {
			if (separator) {
				sb.append(" | ");
			}
			sb.append(translate("table.header.end")).append(": ").append(formattedEndDate);
			separator = true;
		}
		if (overview.getDuration() != null) {
			if (separator) {
				sb.append(" | ");
			}
			sb.append(translate("table.header.duration")).append(": ").append(translate("minutes", new String[] {overview.getDuration().toString()}));
			separator = true;
		}
		
		return sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == nodeLink) {
			String nodeID = (String) nodeLink.getUserObject();
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeID));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == peekViewCtrl) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
