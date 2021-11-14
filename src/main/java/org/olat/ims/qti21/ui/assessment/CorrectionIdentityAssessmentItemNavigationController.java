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
package org.olat.ims.qti21.ui.assessment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.ui.assessment.event.NextAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.event.SelectAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemListEntry;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Container for the assessment form. It contains the previous and next links
 * to catch dirty forms events.
 * 
 * Initial date: 26 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemNavigationController extends BasicController {
	
	private final Link backLink;
	private final Link backOverviewButton;
	private final Link nextItemLink;
	private final Link previousItemLink;
	private final VelocityContainer mainVC;
	
	private final CorrectionIdentityAssessmentItemController itemCtrl;
	
	public CorrectionIdentityAssessmentItemNavigationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			AssessmentItemCorrection itemCorrection, AssessmentItemListEntry assessmentEntry,
			List<? extends AssessmentItemListEntry> assessmentEntryList, CorrectionOverviewModel model,
			GradingTimeRecordRef gradingTimeRecord, boolean readOnly, boolean pageIdentity) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("corrections_navigation");
		
		itemCtrl = new CorrectionIdentityAssessmentItemController(ureq, wControl, testEntry, resolvedAssessmentTest,
				itemCorrection, assessmentEntry, assessmentEntryList, model, gradingTimeRecord, readOnly, pageIdentity);
		listenTo(itemCtrl);
		mainVC.put("items", itemCtrl.getInitialComponent());
		
		backLink = LinkFactory.createLinkBack(mainVC, this);
		backLink.setElementCssClass("o_correction_navigation_back");
		mainVC.put("back", backLink);
		backOverviewButton = LinkFactory.createButton("back.overview", mainVC, this);
		backOverviewButton.setElementCssClass("o_correction_navigation_next");
		mainVC.put("back.overview", backOverviewButton);
		
		previousItemLink = LinkFactory.createButton("previous.item", mainVC, this);
		previousItemLink.setIconLeftCSS("o_icon o_icon_previous");
		previousItemLink.setElementCssClass("o_correction_navigation_previous");
		nextItemLink = LinkFactory.createButton("next.item", mainVC, this);
		nextItemLink.setIconRightCSS("o_icon o_icon_next");
		nextItemLink.setElementCssClass("o_correction_navigation_next");
		
		String[] identityKeys = new String[assessmentEntryList.size()];
		String[] identityValues = new String[assessmentEntryList.size()];
		for(int i=assessmentEntryList.size(); i-->0; ) {
			identityKeys[i] = Integer.toString(i);
			identityValues[i] = assessmentEntryList.get(i).getLabel();
		}
		
		List<SelectPair> itemRefKeys = new ArrayList<>(assessmentEntryList.size());
		for(int i=0; i<assessmentEntryList.size(); i++) {
			String itemTitle = assessmentEntryList.get(i).getLabel();
			itemRefKeys.add(new SelectPair(Integer.toString(i), itemTitle));
		}
		mainVC.contextPut("itemRefKeys", itemRefKeys);
		mainVC.contextPut("selectedValue", assessmentEntryList.indexOf(assessmentEntry));
		
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemListEntry getAssessmentItemSession() {
		return itemCtrl.getAssessmentItemSession();
	}
	
	public List<? extends AssessmentItemListEntry> getAssessmentEntryList() {
		return itemCtrl.getAssessmentEntryList();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(itemCtrl == source) {
			if(event instanceof NextAssessmentItemEvent) {
				doNext(ureq);
			} else if(event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT || event == Event.BACK_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source || backOverviewButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(previousItemLink == source) {
			doPrevious(ureq);
		} else if(nextItemLink == source) {
			doNext(ureq);
		} else if(mainVC == source) {
			String cmd = ureq.getParameter("cid");
			if("sel".equals(cmd)) {
				String selectedKey = ureq.getParameter("item");
				if(StringHelper.isLong(selectedKey)) {
					try {
						doSelectByIndex(ureq, Integer.parseInt(selectedKey));
					} catch (NumberFormatException e) {
						logError("Cannot parse select index: " + selectedKey, e);
					}
				}
			}
		}
	}
	
	protected void updatePreviousNext(String previousText, boolean previousEnable, String nextText, boolean nextEnable) {
		previousItemLink.setCustomDisplayText(previousText);
		previousItemLink.setEnabled(previousEnable);
		nextItemLink.setCustomDisplayText(nextText);
		nextItemLink.setVisible(nextEnable);
		backOverviewButton.setVisible(!nextEnable);
		itemCtrl.updateNext(nextEnable);
		mainVC.setDirty(true);//update the whole navigation bar
	}

	private void doPrevious(UserRequest ureq) {
		AssessmentItemListEntry currentEntry = getAssessmentItemSession();
		List<? extends AssessmentItemListEntry> assessmentEntryList = getAssessmentEntryList();
		int index = assessmentEntryList.indexOf(currentEntry) - 1;
		if(index >= 0 && index < assessmentEntryList.size()) {
			AssessmentItemListEntry nextEntry = assessmentEntryList.get(index);
			fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
		} else {
			previousItemLink.setEnabled(false);
		}
	}
	
	private void doNext(UserRequest ureq) {
		AssessmentItemListEntry currentEntry = getAssessmentItemSession();
		List<? extends AssessmentItemListEntry> assessmentEntryList = getAssessmentEntryList();
		int index = assessmentEntryList.indexOf(currentEntry) + 1;
		if(index >= 0 && index < assessmentEntryList.size()) {
			AssessmentItemListEntry nextEntry = assessmentEntryList.get(index);
			fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
		} else {
			nextItemLink.setVisible(false);
			backOverviewButton.setVisible(true);
			mainVC.setDirty(true);//update the whole navigation bar
		}
	}
	
	private void doSelectByIndex(UserRequest ureq, int index) {
		List<? extends AssessmentItemListEntry> assessmentEntryList = getAssessmentEntryList();
		if(index >= 0 && index < assessmentEntryList.size()) {
			AssessmentItemListEntry nextEntry = assessmentEntryList.get(index);
			fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
		}
	}
	
	public class SelectPair {
		
		private final String text;
		private final String value;
		
		public SelectPair(String value, String text) {
			this.text = text;
			this.value = value;
		}

		public String getText() {
			return text;
		}

		public String getValue() {
			return value;
		}
	}
}
