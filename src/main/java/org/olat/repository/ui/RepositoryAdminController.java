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

import java.util.List;

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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
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
	private MultipleSelectionElement ratingEl;
	private MultipleSelectionElement commentEl;
	private MultipleSelectionElement myCourseSearchEl;
	private SingleSelection taxonomyTreeEl;
	
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyService taxonomyService;
	
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
		
		// Taxonomy
		String selectedTaxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		String[] taxonomyKeys = new String[taxonomyList.size() + 1];
		String[] taxonomyValues = new String[taxonomyList.size() + 1];
		taxonomyKeys[0] = "";
		taxonomyValues[0] = "-";
		for(int i=taxonomyList.size(); i-->0; ) {
			Taxonomy taxonomy = taxonomyList.get(i);
			taxonomyKeys[i + 1] = taxonomy.getKey().toString();
			taxonomyValues[i + 1] = taxonomy.getDisplayName();
		}
		taxonomyTreeEl = uifactory.addDropdownSingleselect("selected.taxonomy.tree", searchCont, taxonomyKeys, taxonomyValues, null);
		taxonomyTreeEl.addActionListener(FormEvent.ONCHANGE);
		boolean found = false;
		if(StringHelper.containsNonWhitespace(selectedTaxonomyTreeKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyTreeKey)) {
					taxonomyTreeEl.select(taxonomyKey, true);
					found = true;
				}
			}
		}
		if(!found && taxonomyKeys.length > 0) {
			taxonomyTreeEl.select(taxonomyKeys[0], true);
		}
		
		// Leave
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
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		//
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
		} else if(taxonomyTreeEl == source) {
			String selectedTaxonomyTreeKey = taxonomyTreeEl.isOneSelected()
					? taxonomyTreeEl.getSelectedKey()
					: null;
			repositoryModule.setTaxonomyTreeKey(selectedTaxonomyTreeKey);
			getWindowControl().setInfo("saved");
		} else if(leaveEl == source) {
			String selectedOption = leaveEl.getSelectedKey();
			RepositoryEntryAllowToLeaveOptions option = RepositoryEntryAllowToLeaveOptions.valueOf(selectedOption);
			repositoryModule.setAllowToLeaveDefaultOption(option);
			getWindowControl().setInfo("saved");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//lifecycleAdminCtrl.formOK(ureq);
	}
}