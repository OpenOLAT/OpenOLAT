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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessRightsEditController extends FormBasicController {
	
	private static final String[] theKeys = new String[]{ "xx" };
	private static final String[] theValues = new String[]{ "" };
	
	private int counter;
	private final Binder binder;
	private final Identity member;
	private BinderAccessRightsRow binderRow;
	
	private final boolean canEdit;
	private final boolean hasButtons;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public AccessRightsEditController(UserRequest ureq, WindowControl wControl, Form form, Binder binder, Identity member) {
		super(ureq, wControl, LAYOUT_CUSTOM, "access_rights", form);
		this.binder = binder;
		this.member = member;
		this.canEdit = true;
		this.hasButtons = false;
		initForm(ureq);
		loadModel();
	}
	
	public AccessRightsEditController(UserRequest ureq, WindowControl wControl, Binder binder, Identity member, boolean canEdit) {
		super(ureq, wControl, "access_rights");
		this.binder = binder;
		this.member = member;
		this.canEdit = canEdit;
		this.hasButtons = true;
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//binder
		MultipleSelectionElement coachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
		MultipleSelectionElement reviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
		binderRow = new BinderAccessRightsRow(coachEl, reviewerEl, binder);
		
		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,SectionAccessRightsRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			MultipleSelectionElement sectionCoachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			MultipleSelectionElement sectionReviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			SectionAccessRightsRow sectionRow = new SectionAccessRightsRow(sectionCoachEl, sectionReviewerEl, section);
			binderRow.getSections().add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);	
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder);
		for(Page page:pages) {
			Section section = page.getSection();
			SectionAccessRightsRow sectionRow = sectionMap.get(section.getKey());
			
			MultipleSelectionElement pageCoachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			MultipleSelectionElement pageReviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			PortfolioElementAccessRightsRow pageRow = new PortfolioElementAccessRightsRow(pageCoachEl, pageReviewerEl, page);
			sectionRow.getPages().add(pageRow);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("binderRow", binderRow);
		}
		
		if(hasButtons) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			buttonsCont.setRootForm(mainForm);
			formLayout.add("buttons", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			if(canEdit) {
				uifactory.addFormSubmitButton("save", buttonsCont);
			}
		}
	}
	
	private void loadModel() {
		if(member != null) {
			List<AccessRights> currentRights = portfolioService.getAccessRights(binder, member);
			
			binderRow.applyRights(currentRights);
			for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
				sectionRow.applyRights(currentRights);
				for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
					pageRow.applyRights(currentRights);
				}
			}
		}
	}
	
	public Identity getMember() {
		return member;
	}
	
	public List<AccessRightChange> getChanges() {
		List<AccessRightChange> changes = new ArrayList<>();
		binderRow.appendChanges(changes, member);
		for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
			sectionRow.appendChanges(changes, member);
			for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
				pageRow.appendChanges(changes, member);
			}
		}
		return changes;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(hasButtons) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		if(hasButtons) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	public static class BinderAccessRightsRow extends PortfolioElementAccessRightsRow {
		
		private final List<SectionAccessRightsRow> sections = new ArrayList<>();

		public BinderAccessRightsRow(MultipleSelectionElement coachEl, MultipleSelectionElement reviewerEl, PortfolioElement element) {
			super(coachEl, reviewerEl, element);
		}

		public List<SectionAccessRightsRow> getSections() {
			return sections;
		}
	}
	
	public static class SectionAccessRightsRow extends PortfolioElementAccessRightsRow {
		
		private final List<PortfolioElementAccessRightsRow> pages = new ArrayList<>();
		
		public SectionAccessRightsRow(MultipleSelectionElement coachEl, MultipleSelectionElement reviewerEl, PortfolioElement element) {
			super(coachEl, reviewerEl, element);
		}
		
		public List<PortfolioElementAccessRightsRow> getPages() {
			return pages;
		}
	}
	
	public static class PortfolioElementAccessRightsRow {
		
		private PortfolioElement element;
		private MultipleSelectionElement coachEl;
		private MultipleSelectionElement reviewerEl;
		
		public PortfolioElementAccessRightsRow(MultipleSelectionElement coachEl, MultipleSelectionElement reviewerEl, PortfolioElement element) {
			this.element = element;
			this.coachEl = coachEl;
			this.reviewerEl = reviewerEl;
			coachEl.setUserObject(Boolean.FALSE);
			reviewerEl.setUserObject(Boolean.FALSE);
		}
		
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			if(coachEl.isAtLeastSelected(1)) {
				if(!Boolean.TRUE.equals(coachEl.getUserObject())) {
					changes.add(new AccessRightChange(PortfolioRoles.coach, element, identity, true));
				}
			} else if(Boolean.TRUE.equals(coachEl.getUserObject())) {
				changes.add(new AccessRightChange(PortfolioRoles.coach, element, identity, false));
			}

			if(reviewerEl.isAtLeastSelected(1)) {
				if(!Boolean.TRUE.equals(reviewerEl.getUserObject())) {
					changes.add(new AccessRightChange(PortfolioRoles.reviewer, element, identity, true));
				}
			} else if(Boolean.TRUE.equals(reviewerEl.getUserObject())) {
				changes.add(new AccessRightChange(PortfolioRoles.reviewer, element, identity, false));
			}
		}
		
		public void applyRights(List<AccessRights> rights) {
			for(AccessRights right:rights) {
				if(element instanceof Page) {
					if(element.getKey().equals(right.getPageKey())) {
						applyRight(right);
					}
				} else if(element instanceof Section) {
					if(element.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
						applyRight(right);
					}
				} else if(element instanceof Binder) {
					if(element.getKey().equals(right.getBinderKey()) && right.getSectionKey() == null && right.getPageKey() == null) {
						applyRight(right);
					}
				}
			}
		}
		
		public void applyRight(AccessRights right) {
			if(right.getRole().equals(PortfolioRoles.coach)) {
				coachEl.select("xx", true);
				coachEl.setUserObject(Boolean.TRUE);
			} else if(right.getRole().equals(PortfolioRoles.reviewer)) {
				reviewerEl.select("xx", true);
				reviewerEl.setUserObject(Boolean.TRUE);
			}
		}
		
		public String getTitle() {
			return element.getTitle();
		}

		public PortfolioElement getElement() {
			return element;
		}

		public MultipleSelectionElement getCoachEl() {
			return coachEl;
		}
		
		public String getCoachComponentName() {
			return coachEl.getComponent().getComponentName();
		}

		public MultipleSelectionElement getReviewerEl() {
			return reviewerEl;
		}
		
		public String getReviewerComponentName() {
			return reviewerEl.getComponent().getComponentName();
		}
	}
}
