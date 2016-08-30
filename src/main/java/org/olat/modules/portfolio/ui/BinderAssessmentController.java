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
package org.olat.modules.portfolio.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AssessmentSectionChange;
import org.olat.modules.portfolio.ui.BinderAssessmentDataModel.AssessmentSectionCols;
import org.olat.modules.portfolio.ui.component.SectionStatusCellRenderer;
import org.olat.modules.portfolio.ui.renderer.PassedCellRenderer;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderAssessmentController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private Binder binder;
	private final BinderSecurityCallback secCallback;
	
	private int counter = 0;
	private FlexiTableElement tableEl;
	private BinderAssessmentDataModel model;
	private FormLayoutContainer buttonsCont;
	
	private boolean withScore;
	private boolean withPassed;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public BinderAssessmentController(UserRequest ureq, WindowControl wControl,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl, "section_assessment");
		this.binder = binder;
		this.secCallback = secCallback;
		this.withPassed = config.isWithPassed();
		this.withScore = config.isWithScore();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentSectionCols.sectionName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentSectionCols.numOfPages));
		if(withPassed) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentSectionCols.passed, new PassedCellRenderer()));
		}
		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentSectionCols.score, new ScoreCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentSectionCols.changeStatus, new SectionStatusCellRenderer(getTranslator())));
		
		model = new BinderAssessmentDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "section-list", model, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEditMode(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "section-assessment");
		
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void loadModel() {
		List<AssessmentSection> aSections = portfolioService.getAssessmentSections(binder, null);
		Map<Section,AssessmentSection> aSectionsMap = new HashMap<>();
		for(AssessmentSection aSection:aSections) {
			aSectionsMap.put(aSection.getSection(), aSection);
		}

		List<Section> sections = portfolioService.getSections(binder);
		List<AssessmentSectionWrapper> rows = new ArrayList<>();
		Map<Section,AssessmentSectionWrapper> sectionToRows = new HashMap<>();
		for(Section section:sections) {
			AssessmentSection assessmentSection = aSectionsMap.get(section);
			AssessmentSectionWrapper row = new AssessmentSectionWrapper(section, assessmentSection);
			sectionToRows.put(section, row);
			rows.add(row);
		}

		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			AssessmentSectionWrapper row = sectionToRows.get(page.getSection());
			if(row != null) {
				row.setNumOfPages(row.getNumOfPages() + 1);
			}
		}
		
		boolean allowedToAssess = false;
		for(AssessmentSectionWrapper row:rows) {
			boolean canAssess = secCallback.canAssess(row.getSection());
			if(canAssess) {
				forgeAssessmentSection(row);
				allowedToAssess = true;
			}
		}
		
		buttonsCont.setVisible(allowedToAssess);
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private void forgeAssessmentSection(AssessmentSectionWrapper row) {
		AssessmentSection assessmentSection = row.getAssessmentSection();

		Section section = row.getSection();
		if(!SectionStatus.isClosed(section)) {
			//score
			String pointVal = null;
			if(assessmentSection != null && assessmentSection.getScore() != null) {
				BigDecimal score = assessmentSection.getScore();
				pointVal = AssessmentHelper.getRoundedScore(score);
			}
	
			TextElement pointEl = uifactory.addTextElement("point" + (++counter), null, 5, pointVal, flc);
			pointEl.setDisplaySize(5);
			row.setScoreEl(pointEl);
			
			//passed
			Boolean passed = assessmentSection == null ? null : assessmentSection.getPassed();
			MultipleSelectionElement passedEl = uifactory.addCheckboxesHorizontal("check" + (++counter), null, flc, onKeys, onValues);
			if(passed != null && passed.booleanValue()) {
				passedEl.select(onKeys[0], passed.booleanValue());
			}
			row.setPassedEl(passedEl);
		}

		if(SectionStatus.isClosed(section)) {
			FormLink reopenButton = uifactory.addFormLink("reopen" + (++counter), "reopen", "reopen", null, flc, Link.BUTTON);
			reopenButton.setUserObject(row);
			row.setButton(reopenButton);
		} else {
			FormLink closeButton = uifactory.addFormLink("close" + (++counter), "close", "close.section", null, flc, Link.BUTTON);
			closeButton.setUserObject(row);
			row.setButton(closeButton);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("close".equals(cmd)) {
				AssessmentSectionWrapper row = (AssessmentSectionWrapper)button.getUserObject();
				doClose(row.getSection());
				loadModel();
			} else if("reopen".equals(cmd)) {
				AssessmentSectionWrapper row = (AssessmentSectionWrapper)button.getUserObject();
				doReopen(row.getSection());
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<AssessmentSectionWrapper> rows = model.getObjects();
		List<Identity> assessedIdentities = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		
		List<AssessmentSectionChange> changes = new ArrayList<>();
		for(AssessmentSectionWrapper row:rows) {
			Section section = row.getSection();
			if(secCallback.canAssess(section) && !SectionStatus.isClosed(section)) {
				BigDecimal score = null;
				if(withScore) {
					String value = row.getScoreEl().getValue();
					if(StringHelper.containsNonWhitespace(value)) {
						score = new BigDecimal(value);
					}
				}
				
				Boolean passed = null;
				if(withPassed) {
					passed = row.getPassedEl().isSelected(0);
				}
				
				for(Identity assessedIdentity:assessedIdentities) {
					changes.add(new AssessmentSectionChange(assessedIdentity, row.getSection(), row.getAssessmentSection(), score, passed));
				}
			}	
		}
		
		portfolioService.updateAssessmentSections(binder, changes, getIdentity());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doClose(Section section) {
		portfolioService.changeSectionStatus(section, SectionStatus.closed, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_CLOSE, getClass(),
				LoggingResourceable.wrap(section));
	}
	
	private void doReopen(Section section) {
		portfolioService.changeSectionStatus(section, SectionStatus.inProgress, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_REOPEN, getClass(),
				LoggingResourceable.wrap(section));
	}

	public static class AssessmentSectionWrapper {
		
		private int numOfPages = 0;
		private Section section;
		private AssessmentSection assessmentSection;
		
		private FormLink button;
		private TextElement scoreEl;
		private MultipleSelectionElement passedEl;
		
		public AssessmentSectionWrapper(Section section, AssessmentSection assessmentSection) {
			this.section = section;
			this.assessmentSection = assessmentSection;
		}
		
		public int getNumOfPages() {
			return numOfPages;
		}
		
		public void setNumOfPages(int numOfPages) {
			this.numOfPages = numOfPages;
		}
		
		public String getSectionTitle() {
			return section.getTitle();
		}
		
		public Boolean getPassed() {
			return assessmentSection == null ? null : assessmentSection.getPassed();
		}
		
		public BigDecimal getScore() {
			return assessmentSection == null ? null : assessmentSection.getScore();
		}

		public Section getSection() {
			return section;
		}

		public void setSection(Section section) {
			this.section = section;
		}

		public AssessmentSection getAssessmentSection() {
			return assessmentSection;
		}

		public void setAssessmentSection(AssessmentSection assessmentSection) {
			this.assessmentSection = assessmentSection;
		}

		public TextElement getScoreEl() {
			return scoreEl;
		}

		public void setScoreEl(TextElement scoreEl) {
			this.scoreEl = scoreEl;
		}

		public MultipleSelectionElement getPassedEl() {
			return passedEl;
		}

		public void setPassedEl(MultipleSelectionElement passedEl) {
			this.passedEl = passedEl;
		}

		public FormLink getButton() {
			return button;
		}

		public void setButton(FormLink button) {
			this.button = button;
		}
	}
}
