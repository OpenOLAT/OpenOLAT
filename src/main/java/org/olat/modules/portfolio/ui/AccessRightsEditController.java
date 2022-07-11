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

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
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
import org.olat.modules.portfolio.ui.event.AccessRightsEvent;
import org.olat.user.DisplayPortraitController;
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
	
	private FormLink removeLink;
	
	private int counter;
	private final Binder binder;
	private final Identity member;
	private BinderAccessRightsRow binderRow;
	private FormLink selectAll, deselectAll;
	
	private final boolean canEdit;
	private final boolean grading;
	private final boolean hasButtons;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public AccessRightsEditController(UserRequest ureq, WindowControl wControl, Form form, Binder binder, Identity member) {
		super(ureq, wControl, LAYOUT_CUSTOM, "access_rights", form);
		this.binder = binder;
		this.member = member;
		this.canEdit = true;
		this.hasButtons = false;
		grading = binder.getTemplate() != null;
		initForm(ureq);
		loadModel();
	}
	
	public AccessRightsEditController(UserRequest ureq, WindowControl wControl, Binder binder, Identity member, boolean canEdit) {
		super(ureq, wControl, "access_rights");
		this.binder = binder;
		this.member = member;
		this.canEdit = canEdit;
		this.hasButtons = true;
		grading = binder.getTemplate() != null;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(member != null && formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;

			Controller portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), member, true, true);
			layoutCont.getFormItemComponent().put("portrait", portraitCtr.getInitialComponent());
			listenTo(portraitCtr);
			Controller userShortDescrCtr = new UserShortDescription(ureq, getWindowControl(), member);
			layoutCont.getFormItemComponent().put("userShortDescription", userShortDescrCtr.getInitialComponent());
			listenTo(userShortDescrCtr);
		}
		
		selectAll = uifactory.addFormLink("form.checkall", "form.checkall", null, formLayout, Link.LINK);
		selectAll.setIconLeftCSS("o_icon o_icon-sm o_icon_check_on");
		deselectAll = uifactory.addFormLink("form.uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		deselectAll.setIconLeftCSS("o_icon o_icon-sm o_icon_check_off");

		//binder
		MultipleSelectionElement coachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
		coachEl.addActionListener(FormEvent.ONCHANGE);
		coachEl.setVisible(grading);
		MultipleSelectionElement reviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
		reviewerEl.addActionListener(FormEvent.ONCHANGE);
		binderRow = new BinderAccessRightsRow(coachEl, reviewerEl, binder);
		coachEl.setUserObject(binderRow);
		reviewerEl.setUserObject(binderRow);
		
		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,SectionAccessRightsRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			MultipleSelectionElement sectionCoachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			sectionCoachEl.addActionListener(FormEvent.ONCHANGE);
			sectionCoachEl.setVisible(grading);
			MultipleSelectionElement sectionReviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			sectionReviewerEl.addActionListener(FormEvent.ONCHANGE);
			SectionAccessRightsRow sectionRow = new SectionAccessRightsRow(sectionCoachEl, sectionReviewerEl, section, binderRow);
			binderRow.getSections().add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);	
			sectionCoachEl.setUserObject(sectionRow);
			sectionReviewerEl.setUserObject(sectionRow);
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder);
		for(Page page:pages) {
			Section section = page.getSection();
			SectionAccessRightsRow sectionRow = sectionMap.get(section.getKey());
			MultipleSelectionElement pageCoachEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			pageCoachEl.addActionListener(FormEvent.ONCHANGE);
			pageCoachEl.setVisible(grading);
			MultipleSelectionElement pageReviewerEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			pageReviewerEl.addActionListener(FormEvent.ONCHANGE);
			PortfolioElementAccessRightsRow pageRow = new PortfolioElementAccessRightsRow(pageCoachEl, pageReviewerEl, page, sectionRow);
			sectionRow.getPages().add(pageRow);
			pageCoachEl.setUserObject(pageRow);
			pageReviewerEl.setUserObject(pageRow);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("binderRow", binderRow);
			layoutCont.contextPut("grading", Boolean.valueOf(grading));
		}
		
		if(hasButtons) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			buttonsCont.setRootForm(mainForm);
			formLayout.add("buttons", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			removeLink = uifactory.addFormLink("remove.all.rights", buttonsCont, Link.BUTTON);
			if(canEdit) {
				uifactory.addFormSubmitButton("save", buttonsCont);
			}
		}
	}
	
	
	public Identity getMember() {
		return member;
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
			binderRow.recalculate();
		}
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeLink == source) {
			fireEvent(ureq, new AccessRightsEvent(AccessRightsEvent.REMOVE_ALL_RIGHTS));
		} else if(selectAll == source) {
			binderRow.setCoach();
			binderRow.recalculate();
		} else if(deselectAll == source) {
			binderRow.unsetCoach();
			binderRow.unsetReviewer();
			for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
				sectionRow.unsetCoach();
				sectionRow.unsetReviewer();
				for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
					pageRow.unsetCoach();
					pageRow.unsetReviewer();
				}
			}
		} else if(source instanceof MultipleSelectionElement) {
			binderRow.recalculate();
		}
		super.formInnerEvent(ureq, source, event);
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
			super(coachEl, reviewerEl, element, null);
		}

		public List<SectionAccessRightsRow> getSections() {
			return sections;
		}
		
		@Override
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			boolean removeCoachRight = false;
			boolean removeReviewerRight = false;
			
			if(getCoachEl().isAtLeastSelected(1)) {
				changes.add(new AccessRightChange(PortfolioRoles.coach, getElement(), identity, true));
				removeReviewerRight = true;
			} else if(getReviewerEl().isAtLeastSelected(1)) {
				changes.add(new AccessRightChange(PortfolioRoles.reviewer, getElement(), identity, true));
				removeCoachRight = true;
			} else {
				removeCoachRight = removeReviewerRight = true;
			}
			
			if(removeCoachRight && getCoachRight() != null) {
				changes.add(new AccessRightChange(PortfolioRoles.coach, getElement(), identity, false));
			}
			if(removeReviewerRight && getReviewerRight() != null) {
				changes.add(new AccessRightChange(PortfolioRoles.reviewer, getElement(), identity, false));
			}
		}
		
		@Override
		public void recalculate() {
			super.recalculate();

			if(sections != null) {
				if(isCoach()) {
					for(SectionAccessRightsRow section:sections) {
						section.setCoach();
					}
				} else if(isReviewer()) {
					for(SectionAccessRightsRow section:sections) {
						section.setReviewer();
					}
				}
				
				for(SectionAccessRightsRow section:sections) {
					section.recalculate();
				}
			}
		}
	}
	
	public static class SectionAccessRightsRow extends PortfolioElementAccessRightsRow {
		
		private final List<PortfolioElementAccessRightsRow> pages = new ArrayList<>();
		
		public SectionAccessRightsRow(MultipleSelectionElement coachEl, MultipleSelectionElement reviewerEl,
				PortfolioElement element, BinderAccessRightsRow parentRow) {
			super(coachEl, reviewerEl, element, parentRow);
		}
		
		public List<PortfolioElementAccessRightsRow> getPages() {
			return pages;
		}
		
		@Override
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			boolean removeCoachRight = false;
			boolean removeReviewerRight = false;
			
			if(getCoachEl().isAtLeastSelected(1)) {
				if(!getParentRow().isCoach()) {
					changes.add(new AccessRightChange(PortfolioRoles.coach, getElement(), identity, true));
				} else {
					removeReviewerRight = true;
				}
			} else if(getReviewerEl().isAtLeastSelected(1)) {
				if(!getParentRow().isCoach() && !getParentRow().isReviewer()) {
					changes.add(new AccessRightChange(PortfolioRoles.reviewer, getElement(), identity, true));
					removeCoachRight = true;
				} else {
					removeReviewerRight = removeCoachRight = true;
				}
			} else {
				removeReviewerRight = removeCoachRight = true;
			}
			
			if(removeCoachRight && getCoachRight() != null) {
				changes.add(new AccessRightChange(PortfolioRoles.coach, getElement(), identity, false));
			}
			if(removeReviewerRight && getReviewerRight() != null) {
				changes.add(new AccessRightChange(PortfolioRoles.reviewer, getElement(), identity, false));
			}
		}

		@Override
		public void recalculate() {
			super.recalculate();
			
			if(pages != null) {
				if(isCoach()) {
					for(PortfolioElementAccessRightsRow page:pages) {
						page.setCoach();
						page.recalculate();
					}
				} else if(isReviewer()) {
					for(PortfolioElementAccessRightsRow page:pages) {
						page.setReviewer();
					}
				}
				
				for(PortfolioElementAccessRightsRow page:pages) {
					page.recalculate();
				}
			}
		}
	}
	
	public static class PortfolioElementAccessRightsRow {
		
		private MultipleSelectionElement coachEl, reviewerEl;
		
		private PortfolioElement element;
		private AccessRights coachRight, reviewerRight;
		private final PortfolioElementAccessRightsRow parentRow;
		
		public PortfolioElementAccessRightsRow(MultipleSelectionElement coachEl, MultipleSelectionElement reviewerEl,
				PortfolioElement element, PortfolioElementAccessRightsRow parentRow) {
			this.element = element;
			this.coachEl = coachEl;
			this.reviewerEl = reviewerEl;
			this.parentRow = parentRow;
		}
		
		public void recalculate() {
			if(isCoach()) {
				if(!isReviewer()) {
					setReviewer();
				}
			}
		}
		
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			boolean removeCoachRight = false;
			boolean removeReviewerRight = false;

			if(coachEl.isAtLeastSelected(1)) {
				if(!parentRow.isCoach() && !parentRow.getParentRow().isCoach()) {
					changes.add(new AccessRightChange(PortfolioRoles.coach, element, identity, true));
					removeReviewerRight = true;
				} else {
					removeCoachRight = removeReviewerRight = true;
				}
			} else if(reviewerEl.isAtLeastSelected(1)) {
				if(!parentRow.isCoach() && !parentRow.getParentRow().isCoach()
						&& !parentRow.isReviewer() && !parentRow.getParentRow().isReviewer()) {
					changes.add(new AccessRightChange(PortfolioRoles.reviewer, element, identity, true));
					removeCoachRight = true;
				} else {
					removeCoachRight = removeReviewerRight = true;
				}
			} else {
				removeCoachRight = removeReviewerRight = true;
			}
			
			if(removeCoachRight && coachRight != null) {
				changes.add(new AccessRightChange(PortfolioRoles.coach, element, identity, false));
			}
			if(removeReviewerRight && reviewerRight != null) {
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
				coachRight = right;
			} else if(right.getRole().equals(PortfolioRoles.reviewer)) {
				reviewerEl.select("xx", true);
				reviewerRight = right;
			}
		}
		
		public String getTitle() {
			return element.getTitle();
		}

		public PortfolioElement getElement() {
			return element;
		}
		
		public PortfolioElementAccessRightsRow getParentRow() {
			return parentRow;
		}
		
		public AccessRights getCoachRight() {
			return coachRight;
		}

		public AccessRights getReviewerRight() {
			return reviewerRight;
		}

		public boolean isCoach() {
			return coachEl.isAtLeastSelected(1);
		}
		
		public void setCoach() {
			coachEl.select(theKeys[0], true);
		}
		
		public void unsetCoach() {
			coachEl.uncheckAll();
		}
		
		public boolean isReviewer() {
			return reviewerEl.isAtLeastSelected(1);
		}
		
		public void setReviewer() {
			reviewerEl.select(theKeys[0], true);
		}
		
		public void unsetReviewer() {
			reviewerEl.uncheckAll();
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
