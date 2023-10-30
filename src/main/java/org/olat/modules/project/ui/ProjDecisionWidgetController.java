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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjDecisionDataModel.DecisionCols;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjDecisionWidgetController extends ProjDecisionListController {
	
	private static final List<DecisionCols> COLS = List.of(DecisionCols.displayName, DecisionCols.decisionDate, DecisionCols.involved);
	private static final Integer NUM_LAST_MODIFIED = 6;
	
	private FormLink titleLink;
	private FormLink createLink;
	private FormLink showAllLink;
	
	public ProjDecisionWidgetController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, "decision_widget", bcFactory, project, secCallback, lastVisitDate, avatarMapperKey);
		initForm(ureq);
		loadModel(ureq, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		titleLink = uifactory.addFormLink("decision.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = bcFactory.getDecisionsUrl(project);
		titleLink.setUrl(url);
		
		createLink = uifactory.addFormLink("decision.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setTitle(translate("decision.create"));
		createLink.setGhost(true);
		createLink.setVisible(secCallback.canCreateDecisions());
		
		showAllLink = uifactory.addFormLink("decision.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		showAllLink.setUrl(url);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == createLink) {
			doCreateDecision(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean isFullTable() {
		return false;
	}

	@Override
	protected boolean isVisible(DecisionCols col) {
		return COLS.contains(col);
	}

	@Override
	protected Integer getNumLastModified() {
		return NUM_LAST_MODIFIED;
	}

	@Override
	protected void onModelLoaded() {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		long count = projectService.getDecisionsCount(searchParams);
		
		showAllLink.setI18nKey(translate("decision.show.all", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}

}
