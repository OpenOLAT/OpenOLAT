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
package org.olat.modules.catalog.ui.admin;

import static org.olat.modules.catalog.launcher.TextLauncherHandler.I18N_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.launcher.TextLauncherHandler;
import org.olat.modules.catalog.launcher.TextLauncherHandler.Config;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jul 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherTextEditController extends AbstractLauncherEditController {
	
	private static final String BUNDLE_NAME = CatalogV2UIFactory.class.getPackageName();
	
	private TabbedPaneItem tabbedPane;
	private RichTextElement defaultLocaleTextEl;
	private int defaultLocaleTabIndex = -1;
	
	private final TextLauncherHandler handler;
	private final String i18nSuffix;
	private List<TranslationItem> translationItems;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	
	public CatalogLauncherTextEditController(UserRequest ureq, WindowControl wControl, TextLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		this.handler = handler;
		this.i18nSuffix = getCatalogLauncher() != null
				? handler.fromXML(getCatalogLauncher().getConfig()).getI18nSuffix()
				: UUID.randomUUID().toString().replace("-", "");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer generalCont) {
		//
	}
	
	@Override
	protected void addFormPart(FormItemContainer formLayout, UserRequest ureq) {
		super.addFormPart(formLayout, ureq);
		
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		
		List<Locale> locales = i18nModule.getEnabledLanguageKeys().stream()
				.map(key -> i18nManager.getLocaleOrNull(key))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		translationItems = new ArrayList<>(locales.size());
		for (int i = 0; i < locales.size(); i++) {
			Locale locale = locales.get(i);
			initTranslationController(ureq, locale, allOverlays.get(locale));
			if (I18nModule.getDefaultLocale().equals(locale)) {
				defaultLocaleTabIndex = i;
			}
		}
		tabbedPane.setSelectedPane(ureq, defaultLocaleTabIndex);
	}

	private void initTranslationController(UserRequest ureq, Locale locale, Locale overlayLocale) {
		String text = i18nManager.getLocalizedString(BUNDLE_NAME, I18N_PREFIX + i18nSuffix, null, locale, true, false);
		
		String elementSuffix = locale.toString();
		FormLayoutContainer cont = FormLayoutContainer.createDefaultFormLayout("trans_" + elementSuffix, getTranslator());
		cont.setRootForm(mainForm);
		tabbedPane.addTab(locale.getDisplayLanguage(getLocale()), cont);
		
		RichTextElement textEl = uifactory.addRichTextElementForStringDataCompact("text" + elementSuffix,
				"launcher.text.text", text, 10, -1, null, cont, ureq.getUserSession(), getWindowControl());
		
		TranslationItem translationItem = new TranslationItem(locale, overlayLocale, textEl);
		translationItems.add(translationItem);
		if (I18nModule.getDefaultLocale().equals(locale)) {
			textEl.setMandatory(true);
			defaultLocaleTextEl = textEl;
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		defaultLocaleTextEl.clearError();
		if(!StringHelper.containsNonWhitespace(defaultLocaleTextEl.getValue())) {
			defaultLocaleTextEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
			tabbedPane.setSelectedPane(ureq, defaultLocaleTabIndex);
		}
		
		return allOk;
	}

	@Override
	protected String getConfig() {
		for (TranslationItem translationItem : translationItems) {
			I18nItem descriptionItem = i18nManager.getI18nItem(BUNDLE_NAME, I18N_PREFIX + i18nSuffix, translationItem.getOverlayLocale());
			i18nManager.saveOrUpdateI18nItem(descriptionItem, translationItem.getTextEl().getValue());
		}
		
		Config config = new Config();
		config.setI18nSuffix(i18nSuffix);
		return handler.toXML(config);
	}
	
	public static class TranslationItem {
		
		private final Locale locale;
		private final Locale overlayLocale;
		private final RichTextElement textEl;
		
		public TranslationItem(Locale locale, Locale overlayLocale, RichTextElement textEl) {
			this.locale = locale;
			this.overlayLocale = overlayLocale;
			this.textEl = textEl;
		}

		public Locale getLocale() {
			return locale;
		}
		
		public Locale getOverlayLocale() {
			return overlayLocale;
		}

		public RichTextElement getTextEl() {
			return textEl;
		}
		
	}

}
