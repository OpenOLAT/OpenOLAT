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
package org.olat.modules.openbadges.ui.element;

import java.text.Collator;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: Sep 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseSelectionSource implements ObjectSelectionSource {

	private final Translator translator;
	private final Collator collator;
	private final Collection<Long> defaultEntryKeys;
	private final Map<Long, RepositoryEntry> keyToCoursesEntry;
	private List<ObjectOption> options;

	public CourseSelectionSource(Locale locale, Collection<Long> defaultEntryKeys, Set<RepositoryEntry> visibleCourses) {
		this.translator = Util.createPackageTranslator(OpenBadgesUIFactory.class, locale);
		this.collator = Collator.getInstance(locale);
		this.defaultEntryKeys = defaultEntryKeys;
		this.keyToCoursesEntry = visibleCourses.stream()
				.collect(Collectors.toMap(RepositoryEntry::getKey, Function.identity(), (u,v) -> v));
		addMissingCourseEntries();
	}

	private void addMissingCourseEntries() {
		if (defaultEntryKeys.isEmpty()) {
			return;
		}
		Set<Long> missingEntryKeys = new HashSet<>(defaultEntryKeys);
		missingEntryKeys.removeAll(keyToCoursesEntry.keySet());
		if (missingEntryKeys.isEmpty()) {
			return;
		}
		
		CoreSpringFactory.getImpl(RepositoryService.class)
				.loadByKeys(missingEntryKeys)
				.forEach(entry -> keyToCoursesEntry.put(entry.getKey(), entry));
	}
	
	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return defaultEntryKeys.stream().map(key -> key.toString()).toList();
	}
	
	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		return getDisplayValue(defaultEntryKeys.stream().map(key -> key.toString()).toList());
	}
	
	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		if (keys.isEmpty()) {
			return ObjectDisplayValues.NONE;
		}
		
		String title = keys.stream()
				.map(key -> keyToCoursesEntry.get(Long.valueOf(key)).getDisplayname())
				.sorted((o1, o2) -> collator.compare(o1, o2))
				.collect(Collectors.joining(", "));
		return toDisplayValues(title);
	}
	
	private ObjectDisplayValues toDisplayValues(String title) {
		return new ObjectDisplayValues(title, EscapeMode.html, title, translator.translate("course.selection.label"));
	}
	
	@Override
	public String getOptionsLabel(Locale locale) {
		return translator.translate("course.selection.option");
	}
	
	@Override
	public List<? extends ObjectOption> getOptions() {
		initOption();
		
		return options;
	}
	
	private void initOption() {
		if (options == null) {
			options = toOptions();
		}
	}
	
	private List<ObjectOption> toOptions() {
		return keyToCoursesEntry.values().stream()
				.map(this::toOption)
				.sorted((o1, o2) -> collator.compare(o1.getTitle(), o2.getTitle()))
				.collect(Collectors.toList());
	}
	
	private ObjectOption toOption(RepositoryEntry courseEntry) {
		String title = courseEntry.getDisplayname() + " · " + courseEntry.getKey();
		return new ObjectOption.ObjectOptionValues(courseEntry.getKey().toString(), title, null, null);
	}
	
	@Override
	public boolean isBrowserAvailable() {
		return true;
	}
	
	@Override
	public ControllerCreator getBrowserCreator() {
		return (UserRequest lureq, WindowControl lwControl) -> 
				new CourseSelectionController(lureq, lwControl);
	}
	
	public static final Set<Long> toKeys(Collection<String> keys) {
		return keys.stream()
				.map(Long::valueOf)
				.collect(Collectors.toSet());
	}

}
