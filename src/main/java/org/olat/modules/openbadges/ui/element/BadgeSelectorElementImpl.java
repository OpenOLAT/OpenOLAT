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
import java.util.Arrays;
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
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-09-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeSelectorElementImpl extends FormItemImpl implements BadgeSelectorElement, FormItemCollection, ControllerEventListener {

	private final BadgeSelectorComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>();
	private final FormLink button;
	private Translator badgesTranslator;

	private BadgeSelectorController badgeSelectorCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private CloseableModalController cmc;

	private final RepositoryEntry entry;
	private final SelectionValues badgesKV;
	private final String mediaUrl;
	private Set<String> selectedRootIds = new HashSet<>();

	public BadgeSelectorElementImpl(UserRequest ureq, WindowControl wControl, String name, RepositoryEntry entry,
									SelectionValues badgesKV, String mediaUrl) {
		super(name);
		this.wControl = wControl;
		this.entry = entry;
		this.badgesKV = badgesKV;
		this.mediaUrl = mediaUrl;
		this.component = new BadgeSelectorComponent(name, this);

		String dispatchId = component.getDispatchID();
		String buttonId = dispatchId + "_bsel";
		button = new FormLinkImpl(buttonId, buttonId, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_badge_selector_button");
		button.getComponent().setLabelCSS("o_badge_selector_span");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(buttonId, button);
		rootFormAvailable(button);
	}

	@Override
	public void setSelection(Collection<String> badgeClassRootIds) {
		selectedRootIds = badgeClassRootIds == null ? new HashSet<>() : new HashSet<>(badgeClassRootIds);
		updateButtonUI();
	}

	@Override
	public Set<String> getSelection() {
		return selectedRootIds;
	}

	public FormLink getButton() {
		return button;
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
		if (button != null && button.getFormDispatchId().equalsIgnoreCase(dispatchuri)) {
			doOpenSelector(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (badgeSelectorCtrl == source) {
			if (event instanceof BadgeSelectorController.BadgesSelectedEvent badgesSelectedEvent) {
				selectedRootIds = badgesSelectedEvent.getRootIds();
				calloutCtrl.deactivate();
				cleanUp();
				updateButtonUI();
			} else if (event == BadgeSelectorController.RESIZED_EVENT) {
				calloutCtrl.handleResized();
			} else if (event == Event.CANCELLED_EVENT) {
				if (calloutCtrl != null) {
					calloutCtrl.deactivate();
				}
				if (cmc != null) {
					cmc.deactivate();
				}
				cleanUp();
			}
		} else if (calloutCtrl == source) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		badgeSelectorCtrl = cleanUp(badgeSelectorCtrl);
		calloutCtrl = cleanUp(calloutCtrl);
		cmc = cleanUp(cmc);
	}

	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}

	private void updateButtonUI() {
		boolean noBadgeSelected = false;
		String linkTitle = badgesKV.keyValues().stream()
				.filter(kv -> selectedRootIds.contains(kv.getKey()))
				.map(SelectionValues.SelectionValue::getValue)
				.sorted(Collator.getInstance(getTranslator().getLocale()))
				.collect(Collectors.joining(", "));
		if (!StringHelper.containsNonWhitespace(linkTitle)) {
			linkTitle = "&nbsp;";
			noBadgeSelected = true;
		}
		if (button != null) {
			button.setI18nKey(linkTitle);
			if (noBadgeSelected) {
				button.setTitle(translator.translate("badge.selector.none"));
			}
		}
	}
	public BadgeSelectorController getSelectorController() {
		return badgeSelectorCtrl;
	}

	private void doOpenSelector(UserRequest ureq) {
		Set<String> availableRootIds = Arrays.stream(badgesKV.keys()).collect(Collectors.toSet());
		badgeSelectorCtrl = new BadgeSelectorController(ureq, wControl, entry, availableRootIds, selectedRootIds);
		badgeSelectorCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, badgeSelectorCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "",
				new CalloutSettings(false, CalloutSettings.CalloutOrientation.bottomOrTop, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}
}
