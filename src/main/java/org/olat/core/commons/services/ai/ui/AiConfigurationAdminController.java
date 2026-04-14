/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.spi.generic.GenericAiSPI;
import org.olat.core.commons.services.ai.spi.generic.GenericAiSpiAdminController;
import org.olat.core.commons.services.ai.spi.generic.GenericAiSpiInstance;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin user interface to configure the AI service. Shows a dropdown to add
 * new providers, per-provider configuration forms with enable/disable toggles
 * and delete buttons, and per-feature configuration (which provider/model to
 * use).
 *
 * Initial date: 22.05.2024<br>
 *
 * @author Florian Gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiConfigurationAdminController extends BasicController {
	@Autowired
	private AiModule aiModule;

	private VelocityContainer mainVC;

	// Add-provider dropdown form
	private AddProviderFormController addProviderFormCtr;

	// SPI config controllers and their associated SPIs
	private final List<Controller> spiConfigCtrs = new ArrayList<>();
	private final Map<Controller, AiSPI> spiByController = new HashMap<>();

	// Features section
	private AiFeaturesAdminController featuresFormCtr;

	// Delete confirmation
	private DialogBoxController confirmDeleteCtrl;
	private AiSPI deleteCandidate;

	public AiConfigurationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("ai_module");
		putInitialPanel(mainVC);

		doInitAddProviderDropdown(ureq);
		doInitSpiConfigControllers(ureq);
		doInitFeaturesController(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == addProviderFormCtr) {
			if ("add-openai".equals(event.getCommand())) {
				doAddOpenAI(ureq);
			} else if ("add-anthropic".equals(event.getCommand())) {
				doAddAnthropic(ureq);
			} else if ("add-generic".equals(event.getCommand())) {
				doAddGeneric(ureq);
			}
		} else if (event == GenericAiApiKeyAdminController.DELETE_EVENT
				|| event == GenericAiSpiAdminController.DELETE_EVENT) {
			doConfirmDelete(ureq, source);
		} else if (source == confirmDeleteCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doDelete(ureq);
			}
			cleanUp();
		} else if (spiByController.containsKey(source)
				&& (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT)) {
			// A provider toggle changed or config saved - refresh features
			doInitFeaturesController(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}

	// ------ Add provider ------

	private void doInitAddProviderDropdown(UserRequest ureq) {
		removeAsListenerAndDispose(addProviderFormCtr);
		addProviderFormCtr = new AddProviderFormController(ureq, getWindowControl());
		listenTo(addProviderFormCtr);
		mainVC.put("addProviderForm", addProviderFormCtr.getInitialComponent());
	}

	private void doAddOpenAI(UserRequest ureq) {
		AiSPI openAiSpi = findSpringProvider("OpenAI");
		if (openAiSpi != null) {
			openAiSpi.setEnabled(true);
			logAudit("AI provider OpenAI added and enabled");
		}
		doRebuildAll(ureq);
	}

	private void doAddAnthropic(UserRequest ureq) {
		AiSPI anthropicSpi = findSpringProvider("Anthropic");
		if (anthropicSpi != null) {
			anthropicSpi.setEnabled(true);
			logAudit("AI provider Anthropic added and enabled");
		}
		doRebuildAll(ureq);
	}

	private void doAddGeneric(UserRequest ureq) {
		GenericAiSPI genericSpi = aiModule.getGenericAiSPI();
		GenericAiSpiInstance instance = genericSpi.createInstance();
		logAudit("Generic AI provider instance created: " + instance.getId());
		doRebuildAll(ureq);
	}

	// ------ Delete provider ------

	private void doConfirmDelete(UserRequest ureq, Controller source) {
		// Determine which SPI the source controller belongs to
		deleteCandidate = findSpiForController(source);
		if (deleteCandidate == null) return;

		String providerName = deleteCandidate.getName();
		String title = translate("ai.delete.confirm.title", providerName);
		String text = translate("ai.delete.confirm.text", providerName);
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
	}

	private void doDelete(UserRequest ureq) {
		if (deleteCandidate == null) return;

		if (deleteCandidate instanceof GenericAiSpiInstance genericInstance) {
			genericInstance.delete();
			logAudit("Generic AI provider instance deleted: " + genericInstance.getId());
		} else {
			// Spring provider: disable and clear API key
			deleteCandidate.setEnabled(false);
			if (deleteCandidate instanceof org.olat.core.commons.services.ai.AiApiKeySPI apiKeySpi) {
				apiKeySpi.setApiKey("");
			}
			logAudit("AI provider disabled and key removed: " + deleteCandidate.getName());
		}

		// Clear feature config if this provider was selected
		String configuredSpiId = aiModule.getMCGeneratorSpiId();
		if (deleteCandidate.getId().equals(configuredSpiId)) {
			aiModule.setMCQuestionGeneratorConfig("", "");
		}
		String configuredImgDescSpiId = aiModule.getImgDescSpiId();
		if (deleteCandidate.getId().equals(configuredImgDescSpiId)) {
			aiModule.setImageDescriptionGeneratorConfig("", "");
		}

		deleteCandidate = null;
		doRebuildAll(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		confirmDeleteCtrl = null;
	}

	// ------ SPI config controllers ------

	private void doInitSpiConfigControllers(UserRequest ureq) {
		for (Controller ctrl : spiConfigCtrs) {
			removeAsListenerAndDispose(ctrl);
		}
		spiConfigCtrs.clear();
		spiByController.clear();

		List<String> spiConfigCtrlNames = new ArrayList<>();

		// Spring providers: show config only when enabled (i.e. "added")
		for (AiSPI spi : aiModule.getSpringProviders()) {
			if (spi.isEnabled()) {
				String ctrlName = "spiConfig_" + spi.getId();
				spiConfigCtrlNames.add(ctrlName);
				Controller ctrl = spi.createAdminController(ureq, getWindowControl());
				listenTo(ctrl);
				spiConfigCtrs.add(ctrl);
				spiByController.put(ctrl, spi);
				mainVC.put(ctrlName, ctrl.getInitialComponent());
			}
		}

		// Generic instances: always shown
		for (GenericAiSpiInstance instance : aiModule.getGenericAiSPI().getInstances()) {
			String ctrlName = "spiConfig_" + instance.getId();
			spiConfigCtrlNames.add(ctrlName);
			Controller ctrl = instance.createAdminController(ureq, getWindowControl());
			listenTo(ctrl);
			spiConfigCtrs.add(ctrl);
			spiByController.put(ctrl, instance);
			mainVC.put(ctrlName, ctrl.getInitialComponent());
		}

		mainVC.contextPut("spiConfigCtrlNames", spiConfigCtrlNames);
	}

	private void doInitFeaturesController(UserRequest ureq) {
		removeAsListenerAndDispose(featuresFormCtr);
		featuresFormCtr = new AiFeaturesAdminController(ureq, getWindowControl());
		listenTo(featuresFormCtr);
		mainVC.put("featuresForm", featuresFormCtr.getInitialComponent());
	}

	private void doRebuildAll(UserRequest ureq) {
		doInitAddProviderDropdown(ureq);
		doInitSpiConfigControllers(ureq);
		doInitFeaturesController(ureq);
	}

	// ------ Helpers ------

	private AiSPI findSpringProvider(String spiId) {
		return aiModule.getSpringProviders().stream()
				.filter(spi -> spi.getId().equals(spiId))
				.findFirst()
				.orElse(null);
	}

	private AiSPI findSpiForController(Controller source) {
		return spiByController.get(source);
	}

	// ------ Inner form for Add Provider dropdown ------

	public class AddProviderFormController extends FormBasicController {

		private FormLink addOpenAiLink;
		private FormLink addAnthropicLink;
		private FormLink addGenericLink;

		public AddProviderFormController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("ai.providers.title");
			setFormDescription("ai.providers.desc");
			setFormInfo("ai.privacy");
			setFormWarning("warn.beta.feature");

			FormLayoutContainer addCont = FormLayoutContainer.createCustomFormLayout("add.cont", getTranslator(),
					velocity_root + "/add_provider.html");
			addCont.setRootForm(mainForm);
			formLayout.add(addCont);

			DropdownItem addDropdown = uifactory.addDropdownMenu("add.provider.menu",
					"ai.add.provider", null, addCont, getTranslator());
			addDropdown.setOrientation(DropdownOrientation.right);
			addDropdown.setIconCSS("o_icon o_icon-fw o_icon_add");

			// OpenAI and Anthropic: greyed out if already configured
			boolean openAiConfigured = isSpringProviderEnabled("OpenAI");
			addOpenAiLink = uifactory.addFormLink("add.openai", "add", "ai.openai.title",
					null, addCont, Link.LINK);
			addOpenAiLink.setEnabled(!openAiConfigured);
			addDropdown.addElement(addOpenAiLink);

			boolean anthropicConfigured = isSpringProviderEnabled("Anthropic");
			addAnthropicLink = uifactory.addFormLink("add.anthropic", "add", "ai.anthropic.title",
					null, addCont, Link.LINK);
			addAnthropicLink.setEnabled(!anthropicConfigured);
			addDropdown.addElement(addAnthropicLink);

			// Separator
			addDropdown.addElement(new SpacerItem("spacer"));

			// Generic: always available
			addGenericLink = uifactory.addFormLink("add.generic", "add", "ai.generic.add",
					null, addCont, Link.LINK);
			addDropdown.addElement(addGenericLink);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == addOpenAiLink) {
				fireEvent(ureq, new Event("add-openai"));
			} else if (source == addAnthropicLink) {
				fireEvent(ureq, new Event("add-anthropic"));
			} else if (source == addGenericLink) {
				fireEvent(ureq, new Event("add-generic"));
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// nothing
		}

		private boolean isSpringProviderEnabled(String spiId) {
			return aiModule.getSpringProviders().stream()
					.anyMatch(spi -> spi.getId().equals(spiId) && spi.isEnabled());
		}
	}
}
