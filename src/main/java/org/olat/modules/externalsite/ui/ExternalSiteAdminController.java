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
package org.olat.modules.externalsite.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.externalsite.ExternalSitesConfigRow;
import org.olat.modules.externalsite.model.ExternalSiteConfiguration;
import org.olat.modules.externalsite.model.ExternalSiteLangConfiguration;
import org.olat.modules.externalsite.ui.ExternalSiteDataModel.ESCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 10, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteAdminController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ExternalSiteAdminController.class);

	private FlexiTableElement tableEl;
	private MultipleSelectionElement iFrameSelectionEl;
	private TextElement iconCssClassEl;
	private TextElement heightEl;
	private FormLayoutContainer tableLayout;

	private final ExternalSiteConfiguration externalSiteConfiguration;
	private ExternalSiteDataModel model;

	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private HttpClientService httpClientService;

	public ExternalSiteAdminController(UserRequest ureq, WindowControl wControl, ExternalSiteConfiguration externalSiteConfiguration) {
		super(ureq, wControl);
		this.externalSiteConfiguration = externalSiteConfiguration;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ESCols.language.i18nKey(), ESCols.language.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ESCols.title.i18nKey(), ESCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ESCols.url.i18nKey(), ESCols.url.ordinal()));

		model = new ExternalSiteDataModel(columnsModel);

		loadModel(ureq);
	}

	private void loadModel(UserRequest ureq) {
		List<ExternalSitesConfigRow> configRows = new ArrayList<>();
		Map<String, ExternalSiteLangConfiguration> langToConfigMap = new HashMap<>();

		if (externalSiteConfiguration.getConfigurationList() != null) {
			for (ExternalSiteLangConfiguration langConfig : externalSiteConfiguration.getConfigurationList()) {
				langToConfigMap.put(langConfig.getLanguage(), langConfig);
			}
		}

		String page = velocity_root + "/lang_options.html";
		tableLayout = FormLayoutContainer.createCustomFormLayout("external.site.options.lang", getTranslator(), page);
		tableLayout.setRootForm(mainForm);
		flc.add(tableLayout);

		for (String langKey : i18nModule.getEnabledLanguageKeys()) {
			ExternalSitesConfigRow row;
			TextElement titleEl = uifactory.addTextElement("externalSiteTitle_" + langKey, "", 32, "", tableLayout);
			titleEl.addActionListener(FormEvent.ONCHANGE);
			TextElement urlEl = uifactory.addTextElement("externalSiteUrl_" + langKey, "", 256, "", tableLayout);
			urlEl.addActionListener(FormEvent.ONCHANGE);
			if (langToConfigMap.containsKey(langKey)) {
				ExternalSiteLangConfiguration langConfig = langToConfigMap.get(langKey);
				titleEl.setValue(langConfig.getTitle());
				urlEl.setValue(langConfig.getExternalUrl());
				langConfig.setDefaultConfiguration(ureq.getLocale().getLanguage().equals(langKey));
				row = new ExternalSitesConfigRow(langConfig, titleEl, urlEl);
			} else {
				row = new ExternalSitesConfigRow(new ExternalSiteLangConfiguration(langKey), titleEl, urlEl);
			}

			configRows.add(row);
		}

		model.setObjects(configRows);

		tableEl = uifactory.addTableElement(getWindowControl(), "externalLanguageTable", model, getTranslator(), tableLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-site-admin");

		String cssClass = externalSiteConfiguration.getNavIconCssClass();
		iconCssClassEl = uifactory.addTextElement("siteIconCssClass", "external.site.icon.css", 32, cssClass, flc);

		iFrameSelectionEl = uifactory.addCheckboxesVertical("externalSiteEnableIFrame", "external.site.enable.iframe", flc, new String[]{"x"}, new String[]{translate("external.site.enable.iframe.info")}, 1);
		iFrameSelectionEl.addActionListener(FormEvent.ONCHANGE);
		iFrameSelectionEl.select("x", externalSiteConfiguration.isExternalUrlInIFrame());

		heightEl = uifactory.addTextElement("externalSiteHeight", "external.site.height", 4, "", flc);
		heightEl.setValue(StringHelper.containsNonWhitespace(externalSiteConfiguration.getExternalSiteHeight())
				? externalSiteConfiguration.getExternalSiteHeight()
				: "800");

		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		flc.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
	}

	private boolean isIFrameEmbeddingAllowed(TextElement urlEl) {
		boolean isAllowed = false;
		HttpPost request = new HttpPost(urlEl.getValue());
		try (CloseableHttpClient client = httpClientService.createHttpClient();
			 CloseableHttpResponse response = client.execute(request)) {
			if (response != null) {
				// check as first instance if CSP contains frame-ancestor which allows iFrame
				isAllowed = Arrays.stream(response.getHeaders("Content-Security-Policy")).filter(csp -> csp.getValue().contains("none")).toList().isEmpty();
				// Check if response header has X-Frame-Options containing "DENY"g
				// this check is more important than frame-ancestor, thus checking as last to ensure iFrame is allowed or not
				isAllowed = Arrays.stream(response.getHeaders("X-Frame-Options")).filter(l -> l.getValue().contains("DENY")).toList().isEmpty();

				// Ensures that the entity content is fully consumed and the content stream, if exists, is closed.
				EntityUtils.consume(response.getEntity());
			}
		} catch (IOException e) {
			log.error("Error creating iFrame Request.", e);
		}
		return isAllowed;
	}

	private boolean validateUrl(TextElement urlEl) {
		boolean allOk = true;

		if (StringHelper.containsNonWhitespace(urlEl.getValue())) {
			try {
				new URL(urlEl.getValue()).toURI();
			} catch (Exception e) {
				urlEl.setErrorKey("error.url.not.valid");
				allOk = false;
			}
		}

		return allOk;
	}

	private boolean validateModelObjects() {
		boolean allOk = true;
		for (ExternalSitesConfigRow row : model.getObjects()) {
			// check if default language is set, which is mandatory
			if (I18nModule.getDefaultLocale().getLanguage().equals(row.getLanguage())
					&& !StringHelper.containsNonWhitespace(row.urlEl().getValue())
					&& !StringHelper.containsNonWhitespace(row.titleEl().getValue())) {
				row.titleEl().setErrorKey("external.site.default.title.missing", row.getLanguage());
				row.urlEl().setErrorKey("external.site.default.url.missing", row.getLanguage());
				allOk = false;
			} else {
				row.titleEl().clearError();
				row.urlEl().clearError();
				// check if url is present but title missing
				if (StringHelper.containsNonWhitespace(row.urlEl().getValue())
						&& !StringHelper.containsNonWhitespace(row.titleEl().getValue())) {
					row.titleEl().setErrorKey("external.site.title.missing");
					allOk = false;
				}
				// check if title is present but url missing
				if (StringHelper.containsNonWhitespace(row.titleEl().getValue())
						&& !StringHelper.containsNonWhitespace(row.urlEl().getValue())) {
					row.urlEl().setErrorKey("external.site.url.missing");
					allOk = false;
				}
				// check if url is valid
				if (StringHelper.containsNonWhitespace(row.urlEl().getValue())) {
					allOk &= validateUrl(row.urlEl());
				}
				// Set error keys for urlEl if iFrame embedding is not allowed
				if (iFrameSelectionEl.isKeySelected("x")
						&& StringHelper.containsNonWhitespace(row.urlEl().getValue())
						&& !row.urlEl().hasError()
						&& !isIFrameEmbeddingAllowed(row.urlEl())) {
					row.urlEl().setErrorKey("external.site.iframe.not.supported");
					allOk = false;
				}
			}
		}
		return allOk;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= model.getObjects().stream().filter(r -> r.urlEl().hasError() || r.titleEl().hasError()).toList().isEmpty();
		allOk &= validateModelObjects();

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == iFrameSelectionEl
				|| source.getName().startsWith("externalSiteTitle_")
				|| source.getName().startsWith("externalSiteUrl_")) {
			if (iFrameSelectionEl.isKeySelected("x")) {
				validateModelObjects();
			} else {
				model.getObjects().forEach(row -> {
					row.urlEl().clearError();
					row.titleEl().clearError();
				});
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	public ExternalSiteConfiguration saveConfiguration() {
		List<ExternalSiteLangConfiguration> langConfigList = new ArrayList<>();
		for (ExternalSitesConfigRow row : model.getObjects()) {
			if (StringHelper.containsNonWhitespace(row.titleEl().getValue())) {
				ExternalSiteLangConfiguration langConfig = row.getRawObject();
				langConfig.setExternalUrlInIFrame(iFrameSelectionEl.isKeySelected("x"));
				langConfigList.add(langConfig);
			}
		}

		externalSiteConfiguration.setNavIconCssClass(iconCssClassEl.getValue());
		externalSiteConfiguration.setExternalUrlInIFrame(iFrameSelectionEl.isKeySelected("x"));
		externalSiteConfiguration.setExternalSiteHeight(heightEl.getValue());
		externalSiteConfiguration.setConfigurationList(langConfigList);
		return externalSiteConfiguration;
	}
}
