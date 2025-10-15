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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesManager.BadgeClassWithSize;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Sep 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BadgeSelectionSource implements ObjectSelectionSource {
	
	private final Translator translator;
	private final Collator collator;
	private final ObjectDisplayValues defaultDisplayValue;
	private final List<String> selectedBadgeClassRootIds;
	private final RepositoryEntry entry;
	private final Set<String> availableRootIds;
	private final String mediaUrl;
	private List<BadgeClassOption> options;
	
	public BadgeSelectionSource(Locale locale, List<String> selectedBadgeClassRootIds, RepositoryEntry entry, SelectionValues badgesKV, String mediaUrl) {
		this.translator = Util.createPackageTranslator(OpenBadgesUIFactory.class, locale);
		this.collator = Collator.getInstance(locale);
		this.defaultDisplayValue = initDefaultDisplayValue(badgesKV, selectedBadgeClassRootIds);
		this.selectedBadgeClassRootIds = selectedBadgeClassRootIds;
		this.entry = entry;
		this.availableRootIds = Arrays.stream(badgesKV.keys()).collect(Collectors.toSet());
		this.mediaUrl = mediaUrl;
	}
	
	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return selectedBadgeClassRootIds;
	}
	
	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		return defaultDisplayValue;
	}
	
	private ObjectDisplayValues initDefaultDisplayValue(SelectionValues badgesKV, List<String> selectedBadgeClassRootIds) {
		String title = selectedBadgeClassRootIds.stream()
				.map(key -> badgesKV.getValue(key))
				.filter(Objects::nonNull)
				.sorted((o1, o2) -> collator.compare(o1, o2))
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(title, title);
	}
	
	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		initOptions();
		
		String title = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(BadgeClassOption::getDisplayTitle)
				.sorted((o1, o2) -> collator.compare(o1, o2))
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(title, title);
	}
	
	@Override
	public String getOptionsLabel(Locale locale) {
		return translator.translate("class.selection.option");
	}
	
	@Override
	public List<? extends ObjectOption> getOptions() {
		initOptions();
		
		return options;
	}
	
	private void initOptions() {
		if (options != null) {
			return;
		}
		
		options = CoreSpringFactory.getImpl(OpenBadgesManager.class).getBadgeClassesWithSizes(entry).stream()
				.filter(bcs -> availableRootIds.contains(bcs.badgeClass().getRootId()))
				.map(this::toOption)
				.sorted((o1, o2) ->  collator.compare(o1.getTitle(), o2.getTitle()))
				.toList();
	}
	
	private BadgeClassOption toOption(BadgeClassWithSize bcs) {
		BadgeClass badgeClass = bcs.badgeClass();
		
		String title = badgeClass.getName() + " · " +  translator.translate("form.version") + " " + badgeClass.getVersion();
		String subTitle = translator.translate("class.status." + badgeClass.getStatus().name());
		String imageSrc = mediaUrl + "/" + badgeClass.getImage();
		String imageAlt = translator.translate("badge.image");
		return new BadgeClassOption(badgeClass.getRootId().toString(), title, subTitle, null, imageSrc, imageAlt, badgeClass.getName());
	}
	
	static final class BadgeClassOption extends ObjectOptionValues {
		
		private final String displayTitle;
		
		public BadgeClassOption(String key, String title, String subTitle, String subTitleFull, String imageSrc, String imageAlt, String displayTitle) {
			super(key, title, subTitle, subTitleFull, imageSrc, imageAlt);
			this.displayTitle = displayTitle;
		}
		
		public String getDisplayTitle() {
			return displayTitle;
		}
		
	}
	
	@Override
	public boolean isBrowserAvailable() {
		return false;
	}
	
	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection) {
		return null;
	}

}
