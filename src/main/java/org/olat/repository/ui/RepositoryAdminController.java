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
package org.olat.repository.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryAdminController extends FormBasicController {

	private static final String[] keys = {"on"};
	private static final String[] leaveKeys = {
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
	};
	
	private SingleSelection leaveEl;
	private MultipleSelectionElement myCourseSearchEl, commentEl, ratingEl;
	
	private RepositoryLifecycleAdminController lifecycleAdminCtrl;
	
	@Autowired
	private RepositoryModule repositoryModule;
	
	public RepositoryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createDefaultFormLayout("search", getTranslator());
		searchCont.setFormContextHelp("Modules: Repository");
		searchCont.setFormTitle(translate("repository.admin.title"));
		formLayout.add(searchCont);
		searchCont.setRootForm(mainForm);

		boolean searchEnabled = repositoryModule.isMyCoursesSearchEnabled();
		String[] values = new String[] { translate("on") };
		myCourseSearchEl = uifactory.addCheckboxesHorizontal("my.course.search.enabled", searchCont, keys, values);
		myCourseSearchEl.addActionListener(FormEvent.ONCHANGE);
		myCourseSearchEl.select(keys[0], searchEnabled);
		
		boolean commentEnabled = repositoryModule.isCommentEnabled();
		commentEl = uifactory.addCheckboxesHorizontal("my.course.comment.enabled", searchCont, keys, values);
		commentEl.addActionListener(FormEvent.ONCHANGE);
		commentEl.select(keys[0], commentEnabled);
		
		boolean ratingEnabled = repositoryModule.isRatingEnabled();
		ratingEl = uifactory.addCheckboxesHorizontal("my.course.rating.enabled", searchCont, keys, values);
		ratingEl.addActionListener(FormEvent.ONCHANGE);
		ratingEl.select(keys[0], ratingEnabled);
		
		FormLayoutContainer leaveCont = FormLayoutContainer.createDefaultFormLayout("leave", getTranslator());
		leaveCont.setFormTitle(translate("repository.admin.leave.title"));
		formLayout.add(leaveCont);
		leaveCont.setRootForm(mainForm);
		
		String[] leaveValues = new String[] {
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		leaveEl = uifactory.addDropdownSingleselect("leave.courses", "repository.admin.leave.label", leaveCont, leaveKeys, leaveValues, null);
		leaveEl.addActionListener(FormEvent.ONCHANGE);
		RepositoryEntryAllowToLeaveOptions leaveOption = repositoryModule.getAllowToLeaveDefaultOption();
		if(leaveOption != null) {
			leaveEl.select(leaveOption.name(), true);
		} else {
			leaveEl.select(RepositoryEntryAllowToLeaveOptions.atAnyTime.name(), true);
		}
		
		lifecycleAdminCtrl = new RepositoryLifecycleAdminController(ureq, getWindowControl(), mainForm);
		listenTo(lifecycleAdminCtrl);
		formLayout.add(lifecycleAdminCtrl.getInitialFormItem());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= lifecycleAdminCtrl.validateFormLogic(ureq);
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(myCourseSearchEl == source) {
			boolean on = !myCourseSearchEl.getSelectedKeys().isEmpty();
			repositoryModule.setMyCoursesSearchEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(commentEl == source) {
			boolean on = !commentEl.getSelectedKeys().isEmpty();
			repositoryModule.setCommentEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(ratingEl == source) {
			boolean on = !ratingEl.getSelectedKeys().isEmpty();
			repositoryModule.setRatingEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(leaveEl == source) {
			String selectedOption = leaveEl.getSelectedKey();
			RepositoryEntryAllowToLeaveOptions option = RepositoryEntryAllowToLeaveOptions.valueOf(selectedOption);
			repositoryModule.setAllowToLeaveDefaultOption(option);
			getWindowControl().setInfo("saved");
		}
		lifecycleAdminCtrl.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lifecycleAdminCtrl.formOK(ureq);
	}
}