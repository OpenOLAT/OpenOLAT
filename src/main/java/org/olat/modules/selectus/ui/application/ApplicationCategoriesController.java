/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoriesController extends FormBasicController {
	
	private TagSelection tagsEl;
	
	private Position position;
	private Application application;
	private final boolean canEdit;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private TaggingService taggingService;
	
	public ApplicationCategoriesController(UserRequest ureq, WindowControl wControl, Position position, Application application,
			 RecruitingPositionSecurityCallback secCallback, Form rootForm) {
		super(ureq, wControl, "categories", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		canEdit = secCallback.canEditApplicationCategories() || secCallback.canEditApplicationAdministrativeCategories();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<TagInfo> selectedTags = getTags();
		List<TagInfo> allTags = getAllTags(selectedTags);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), allTags);
		tagsEl.setSelectedTags(selectedTags);
		tagsEl.addActionListener(FormEvent.ONCHANGE);
		tagsEl.setCanCreateNewTags(secCallback.canEditPositionCategories());
		tagsEl.setDirtyCheck(false);
		tagsEl.setEnabled(canEdit);
		
		if(canEdit) {
			String hint = translate("edit.categories.details.hint");
			hint += "<br>" + translate("add.categories.details.hint");
			if(secCallback.canEditPositionCategories()) {
				hint += "<br>" + translate("add.categories.plural.new.hint");
			}
			if(secCallback.canEditApplicationAdministrativeCategories()) {
				hint += "<br>" + translate("add.categories.plural.administrative.hint");
			}
			hint += "<br>" + translate("remove.categories.details.hint");
			tagsEl.setHelpText(hint);
			formLayout.contextPut("hint", hint);
		}
	}
	
	private List<TagInfo> getTags() {
		List<ApplicationCategoryInfos> currentCategories = taggingService.getApplicationCategories(position, application,
				secCallback.canSeeApplicationAdministrativeCategories());
		boolean canEditAdministrative = secCallback.canEditApplicationAdministrativeCategories();
		return currentCategories.stream()
				.map(cat -> new ApplicationTagInfoImpl(cat, true, canEditAdministrative || !cat.isAdministrative()))
				.collect(Collectors.toList());
	}
	
	private List<TagInfo> getAllTags(List<TagInfo> currentTags) {
		List<TagInfo> items = new ArrayList<>();
		
		List<Category> allCategories = taggingService.getAvailableCategoriesFor(position);
		if(secCallback.canSeeApplicationAdministrativeCategories()) {
			for(Category category:allCategories) {
				items.add(new ApplicationTagInfoImpl(category, true, secCallback.canEditApplicationAdministrativeCategories()));
			}
		}
		for(Category category:allCategories) {
			items.add(new ApplicationTagInfoImpl(category, false, canEdit));
		}
		// categories of the application which are not available anymore
		Set<Long> keys = items.stream().map(TagInfo::getKey).collect(Collectors.toSet());
		for(TagInfo currentTag:currentTags) {
			if(keys.add(currentTag.getKey())) {
				items.add(currentTag);
			}
		}
		Collections.sort(items, new ApplicationTagInfoComparator());
		return items;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tagsEl == source) {
			doSaveTags(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSaveTags(UserRequest ureq) {
		if(!canEdit) return;
		
		List<String> selectedCategories = tagsEl.getDisplayNames();
		taggingService.setCategories(application, selectedCategories, secCallback.canEditApplicationAdministrativeCategories(),
				position, getIdentity(), getLocale());
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
