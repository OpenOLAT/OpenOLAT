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
package org.olat.modules.openbadges.ui.element;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.repository.model.RepositoryEntryRefImpl;

/**
 * Initial date: 2024-09-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseSelectorElementImpl extends FormItemImpl implements CourseSelectorElement,
		FormItemCollection, ControllerEventListener  {

	private final CourseSelectorComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>();
	private final FormLink button;
	private Translator badgesTranslator;

	private CourseSelectorController courseSelectorCtrl;
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController courseBrowserCtrl;

	private final Set<RepositoryEntry> visibleCourses;
	private Set<Long> selectedKeys = new HashSet<>();

	public CourseSelectorElementImpl(WindowControl wControl, String name, Set<RepositoryEntry> visibleCourses) {
		super(name);
		this.wControl = wControl;
		this.visibleCourses = visibleCourses;
		this.component = new CourseSelectorComponent(name, this);

		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_course_selector";
		button = new FormLinkImpl(id, id, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_badge_course_selector_button");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(id, button);
		rootFormAvailable(button);
	}

	@Override
	public void setCourses(Collection<? extends RepositoryEntryRef> courses) {
		selectedKeys = courses == null ? new HashSet<>() : courses.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
		updateButtonUI(false);
	}

	@Override
	public Set<RepositoryEntryRef> getCourses() {
		return selectedKeys.stream().map(RepositoryEntryRefImpl::new).collect(Collectors.toSet());
	}

	public FormLink getButton() {
		return button;
	}

	public CourseSelectorController getSelectorController() {
		return courseSelectorCtrl;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(button);
		badgesTranslator = Util.createPackageTranslator(OpenBadgesUIFactory.class, getTranslator().getLocale());
	}

	private void rootFormAvailable(FormLink button) {
		if (button != null && button.getRootForm() != getRootForm()) {
			button.setRootForm(getRootForm());
		}
	}

	@Override
	public void reset() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if (button != null && button.getFormDispatchId().equals(dispatchuri)) {
			doOpenSelector(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (courseSelectorCtrl == source) {
			if (event instanceof CourseSelectorController.ApplyEvent applyEvent) {
				selectedKeys = applyEvent.getKeys();
				cmc.deactivate();
				cleanUp();
				updateButtonUI(true);
			} else if (event == CourseSelectorController.BROWSE_EVENT) {
				cmc.deactivate();
				cleanUp();
				doOpenBrowser(ureq);
			}
		} else if (courseBrowserCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repositoryEntry = courseBrowserCtrl.getSelectedEntry();
				selectedKeys = Set.of(repositoryEntry.getKey());
				updateButtonUI(true);
			} else if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				selectedKeys = courseBrowserCtrl.getSelectedEntries().stream().map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
				updateButtonUI(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		courseSelectorCtrl = cleanUp(courseSelectorCtrl);
		courseBrowserCtrl = cleanUp(courseBrowserCtrl);
		cmc = cleanUp(cmc);
	}

	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}

	private void updateButtonUI(boolean setDirty) {
		String linkTitle = visibleCourses.stream()
				.filter(course -> selectedKeys.contains(course.getKey()))
				.map(course -> StringHelper.escapeHtml(course.getDisplayname()))
				.sorted(Collator.getInstance(getTranslator().getLocale())).collect(Collectors.joining(", "));
		button.setI18nKey(linkTitle);

		if (setDirty) {
			getFormItemComponent().setDirty(true);
			Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
			wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
		}
	}

	private void doOpenSelector(UserRequest ureq) {
		courseSelectorCtrl = new CourseSelectorController(ureq, wControl, visibleCourses, selectedKeys);
		courseSelectorCtrl.addControllerListener(this);

		cmc = new CloseableModalController(wControl, getTranslator().translate("close"),
				courseSelectorCtrl.getInitialComponent());
		cmc.activate();
		cmc.addControllerListener(this);
	}

	private void doOpenBrowser(UserRequest ureq) {
		courseBrowserCtrl = new ReferencableEntriesSearchController(wControl, ureq,
				new String[] {CourseModule.ORES_TYPE_COURSE},
				(re) -> RepositoryEntryStatusEnum.published.equals(re.getEntryStatus()), null,
				badgesTranslator.translate("course.selector.add"), false, false,
				true, false, true, false,
				RepositorySearchController.Can.referenceable);
		courseBrowserCtrl.addControllerListener(this);

		String title = badgesTranslator.translate("course.selector.add");
		cmc = new CloseableModalController(wControl, translator.translate("close"),
				courseBrowserCtrl.getInitialComponent(), true, title);
		cmc.activate();
		cmc.addControllerListener(this);
	}
}
