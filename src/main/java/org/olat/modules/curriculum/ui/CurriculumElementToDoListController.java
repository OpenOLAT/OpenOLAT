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
package org.olat.modules.curriculum.ui;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.AccessibleCurriculumSearchParams;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementToDoListController extends ToDoTaskListController {
	
	private static final String GUIPREF_KEY_LAST_VISIT = "curriculum.element.todos.last.visit";

	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private FormLink createLink;

	private CloseableModalController cmc;
	private Controller toDoCreateCtrl;

	private final CurriculumElement element;
	private final CurriculumSecurityCallback secCallback;
	private List<String> allLevelsSubPaths;

	private Date lastVisitDate;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementToDoListController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_todos", CurriculumElementToDoProvider.TYPE,
				element.getCurriculum().getKey(), element.getKey().toString());
		this.element = element;
		this.secCallback = secCallback;
		
		AccessibleCurriculumSearchParams searchParams = new AccessibleCurriculumSearchParams(getIdentity());
		searchParams.setIncludeImplementationOwnership(false);
		searchParams.setCurriculums(List.of(element.getCurriculum()));
		Set<Long> accessibleElementKeys = curriculumService.getAccessibleCurriculumKeys(searchParams).curriculumElementKeys();
		List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(element);
		descendants.add(element);
		allLevelsSubPaths = descendants.stream()
				.filter(level -> accessibleElementKeys.contains(level.getKey()))
				.map(CurriculumElement::getKey)
				.map(String::valueOf)
				.toList();
		
		if (allLevelsSubPaths.isEmpty()) {
			// Not existing key to prevent loading all to-dos.
			allLevelsSubPaths = List.of("-1");
		}
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			String implementationKey = element.getKey().toString();
			if (element.getImplementation() != null) {
				implementationKey = element.getImplementation().getKey().toString();
			}
			if (StringHelper.containsNonWhitespace(implementationKey)) {
				String guiPrefsKey = GUIPREF_KEY_LAST_VISIT + "::" + implementationKey;
				Object pref = guiPrefs.get(CurriculumElementToDoListController.class, guiPrefsKey);
				if (pref instanceof String prefDate) {
					try {
						lastVisitDate = Formatter.parseDatetime(prefDate);
					} catch (ParseException e) {
						//
					}
				}
				
				String lastVisit = Formatter.formatDatetime(new Date());
				guiPrefs.putAndSave(CurriculumElementToDoListController.class, guiPrefsKey, lastVisit);
			}
		}

		initForm(ureq);

		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "curriculum-element-todos");

		reload(ureq);
	}

	@Override
	protected Date getNewSinceDate() {
		// Only editors see the "new" label
		return secCallback.canEditCurriculumElement(element)? lastVisitDate: null;
	}

	@Override
	protected boolean isFilterMyEnabled() {
		return true;
	}

	@Override
	protected boolean isShowContextInEditDialog() {
		return false;
	}

	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.contextType;
	}

	@Override
	protected boolean isDefaultVisible(ToDoTaskCols col) {
		if (col == ToDoTaskCols.contextTitle) {
			return false;
		}
		if (col == ToDoTaskCols.contextSubTitle) {
			return true;
		}
		return super.isDefaultVisible(col);
	}

	@Override
	protected String getColumnLabel(ToDoTaskCols col) {
		return switch (col) {
			case contextTitle -> translate("curriculum.title");
			case contextSubTitle -> translate("curriculum.element.todo.element");
			default -> null;
		};
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		return toDoService.getTagInfos(tagSearchParams, null);
	}

	@Override
	protected boolean isFilterTabUnassignedEnabled() {
		return true;
	}

	@Override
	protected Collection<String> getTypes() {
		return List.of(CurriculumElementToDoProvider.TYPE);
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		params.setOriginIds(List.of(element.getCurriculum().getKey()));
		boolean oneLevelOnly = thisLevelButton.getComponent().isPrimary();
		if (oneLevelOnly) {
			List<String> thisLevelSubPaths = allLevelsSubPaths.contains(element.getKey().toString())
					? List.of(element.getKey().toString())
					: List.of("-1");
			params.setOriginSubPaths(thisLevelSubPaths);
		} else {
			params.setOriginSubPaths(allLevelsSubPaths);
		}
		return params;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return new CurriculumElementToDoSecurityCallback(secCallback, element);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_structure");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");

		if (secCallback.canEditCurriculumElement(element)) {
			createLink = uifactory.addFormLink("curriculum.element.todo.create", formLayout, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon_add");
		}

		super.initForm(formLayout, listener, ureq);
		updateUI();
	}

	private void updateUI() {
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		CurriculumElement effectiveRoot = implementation != null ? implementation : element;
		CurriculumElementType type = effectiveRoot.getType();
		boolean isStructuredProduct = type == null || !type.isSingleElement();
		allLevelsButton.setVisible(isStructuredProduct);
		thisLevelButton.setVisible(isStructuredProduct);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			super.doCreateToDoTask(ureq);
		} else if (source == allLevelsButton) {
			doToggleLevels(ureq, false);
		} else if (source == thisLevelButton) {
			doToggleLevels(ureq, true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doToggleLevels(UserRequest ureq, boolean thisLevel) {
		allLevelsButton.setPrimary(!thisLevel);
		thisLevelButton.setPrimary(thisLevel);
		reload(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == toDoCreateCtrl) {
			if (event == Event.DONE_EVENT) {
				reload(ureq);
			}
			cmc.deactivate();
			cleanUpCreate();
		} else if (source == cmc) {
			cleanUpCreate();
		}
		super.event(ureq, source, event);
	}

	private void cleanUpCreate() {
		removeAsListenerAndDispose(toDoCreateCtrl);
		removeAsListenerAndDispose(cmc);
		toDoCreateCtrl = null;
		cmc = null;
	}

	@Override
	protected void doPrimaryTableAction(UserRequest ureq) {
		doCreateToDoTask(ureq);
	}

}
