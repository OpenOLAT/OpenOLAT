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
package org.olat.user;

import static org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption.ObjectOptionValues.CSS_TITLE_ONLY;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectDisplayValues;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 21 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IdentitySelectionSource implements ObjectSelectionSource {

	private final Locale locale;
	private final Collator collator;
	private final Collection<? extends Identity> selectedIdentities;
	private final Supplier<Collection<Identity>> identitiesSupplier;
	private Collection<Identity> identities;

	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;

	public IdentitySelectionSource(Locale locale, Collection<? extends Identity> selectedIdentities,
			Supplier<Collection<Identity>> identitiesSupplier) {
		CoreSpringFactory.autowireObject(this);
		this.locale = locale;
		this.collator = Collator.getInstance(locale);
		this.selectedIdentities = selectedIdentities;
		this.identitiesSupplier = identitiesSupplier;
	}

	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return selectedIdentities.stream()
				.map(identity -> identity.getKey().toString())
				.toList();
	}

	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		if (selectedIdentities == null || selectedIdentities.isEmpty()) {
			return ObjectDisplayValues.NONE;
		}
		return joinNames(selectedIdentities.stream());
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return ObjectDisplayValues.NONE;
		}
		Set<Long> identityKeys = keys.stream().map(Long::valueOf).collect(Collectors.toSet());
		return joinNames(getIdentities().stream().filter(identity -> identityKeys.contains(identity.getKey())));
	}

	private ObjectDisplayValues joinNames(Stream<? extends Identity> stream) {
		String joined = stream
				.map(userManager::getUserDisplayName)
				.sorted(collator::compare)
				.collect(Collectors.joining(", "));
		return new ObjectDisplayValues(joined, joined);
	}

	@Override
	public String getOptionsLabel(Locale locale) {
		return Util.createPackageTranslator(IdentitySelectionSource.class, locale)
				.translate("identity.selection.options.label");
	}

	@Override
	public List<? extends ObjectOption> getOptions() {
		Collection<Identity> identities = getIdentities();
		if (identities == null || identities.isEmpty()) {
			return List.of();
		}
		Map<Long, PortraitUser> portraitUsersByKey = userPortraitService.createPortraitUsers(locale, identities).stream()
				.collect(Collectors.toMap(PortraitUser::getIdentityKey, Function.identity()));

		return identities.stream()
				.map(identity -> toOption(identity, portraitUsersByKey.get(identity.getKey())))
				.sorted((o1, o2) -> collator.compare(o1.getTitle(), o2.getTitle()))
				.toList();
	}

	private Collection<Identity> getIdentities() {
		if (identities == null) {
			identities = identitiesSupplier.get();
			if (selectedIdentities != null && !selectedIdentities.isEmpty()) {
				Set<Long> loadedKeys = identities.stream().map(Identity::getKey).collect(Collectors.toSet());
				List<? extends Identity> missing = selectedIdentities.stream()
						.filter(i -> !loadedKeys.contains(i.getKey()))
						.toList();
				if (!missing.isEmpty()) {
					List<Identity> result = new ArrayList<>(identities);
					result.addAll(missing);
					identities = result;
				}
			}
		}
		return identities;
	}

	private ObjectOption.ObjectOptionValues toOption(Identity identity, PortraitUser portraitUser) {
		String key = identity.getKey().toString();
		String displayName = userManager.getUserDisplayName(identity);

		UserPortraitComponent comp = new UserPortraitComponent("upc_" + identity.getKey(), locale);
		comp.setSize(PortraitSize.small);
		comp.setPortraitUser(portraitUser);

		StringOutput sb = new StringOutput();
		comp.getHTMLRendererSingleton().render(null, sb, comp, null, null, null, null);

		return new ObjectOption.ObjectOptionValues(key, CSS_TITLE_ONLY, displayName, null, null, null, null, sb.toString());
	}

	@Override
	public boolean isBrowserAvailable() {
		return false;
	}

	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection) {
		return null;
	}

	public static List<IdentityRef> toRefs(Collection<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return List.of();
		}
		return keys.stream()
				.map(Long::valueOf)
				.map(key -> (IdentityRef) () -> key)
				.toList();
	}
}
