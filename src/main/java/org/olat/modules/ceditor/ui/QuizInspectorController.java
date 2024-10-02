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
package org.olat.modules.ceditor.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.QuizElement;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizInspectorController extends FormBasicController implements PageElementInspectorController {
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private QuizElement quizElement;
	private final PageElementStore<QuizElement> store;
	private final RepositoryEntry entry;
	private TextElement titleEl;
	private TextAreaElement descriptionEl;
	private StaticTextElement imageNameEl;
	private FormLink chooseImageButton;
	private CloseableModalController cmc;
	private ChooseImageController chooseImageController;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public QuizInspectorController(UserRequest ureq, WindowControl wControl, QuizElement quizElement,
								   PageElementStore<QuizElement> store, RepositoryEntry entry) {
		super(ureq, wControl, "tabs_inspector");
		this.quizElement = quizElement;
		this.store = store;
		this.entry = entry;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.quiz");
	}

	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addQuizTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addQuizTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("quiz", getTranslator());
		layoutCont.setElementCssClass("o_quiz_inspector_tab");
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.quiz"), layoutCont);

		titleEl = uifactory.addTextElement("title", 80, "", layoutCont);
		titleEl.addActionListener(FormEvent.ONCHANGE);

		descriptionEl = uifactory.addTextAreaElement("description", "quiz.description", -1, -1, -1, false, false, null, layoutCont);
		descriptionEl.addActionListener(FormEvent.ONCHANGE);

		String page = velocity_root + "/quiz_tab_image.html";
		FormLayoutContainer searchLayout = FormLayoutContainer.createCustomFormLayout("image.search", getTranslator(), page);
		layoutCont.add(searchLayout);
		searchLayout.setLabel("background.image", null);
		imageNameEl = uifactory.addStaticTextElement("imageName", "", "", searchLayout);

		chooseImageButton = uifactory.addFormLink("chooseImageLink", "", "", searchLayout, Link.NONTRANSLATED);
		chooseImageButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		String chooseImageLabel = getTranslator().translate("choose.image");
		chooseImageButton.setLinkTitle(chooseImageLabel);
		chooseImageButton.setEnabled(true);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(getQuizSettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory,
				getLayoutSettings(getQuizSettings()), velocity_root);
	}

	private void updateUI() {
		QuizSettings quizSettings = quizElement.getSettings();
		titleEl.setValue(quizSettings.getTitle());
		descriptionEl.setValue(quizSettings.getDescription());
		if (quizElement instanceof QuizPart quizPart) {
			if (quizPart.getBackgroundImageMedia() != null) {
				String imageName = quizPart.getBackgroundImageMedia().getTitle();
				imageNameEl.setValue(imageName);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (cmc == source) {
			cleanUp();
		} else if (chooseImageController == source) {
			if (event == Event.DONE_EVENT) {
				doSaveMedia(ureq, chooseImageController.getMediaReference());
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(chooseImageController);
		cmc = null;
		chooseImageController = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		} else if (titleEl == source) {
			doSaveSettings(ureq);
		} else if (descriptionEl == source) {
			doSaveSettings(ureq);
		} else if (chooseImageButton == source) {
			doChooseImage(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private void doSaveSettings(UserRequest ureq) {
		QuizSettings quizSettings = quizElement.getSettings();
		quizSettings.setTitle(StringHelper.xssScan(titleEl.getValue()));
		quizSettings.setDescription(StringHelper.xssScan(descriptionEl.getValue()));
		quizElement.setSettings(quizSettings);
		store.savePageElement(quizElement);
		dbInstance.commit();
		updateUI();
		fireEvent(ureq, new ChangePartEvent(quizElement));
	}

	private void doSaveMedia(UserRequest ureq, Media media) {
		if (quizElement instanceof QuizPart quizPart) {
			quizPart.setBackgroundImageMedia(media);
			quizPart.setBackgroundImageMediaVersion(media.getVersions().get(0));
			quizPart.setBackgroundImageIdentity(getIdentity());
			quizElement = store.savePageElement(quizElement);
			dbInstance.commit();
			updateUI();
			fireEvent(ureq, new ChangePartEvent(quizElement));
		}
	}

	private void doSave(UserRequest ureq) {
		quizElement = store.savePageElement(quizElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(quizElement));
	}

	private void doChooseImage(UserRequest ureq) {
		chooseImageController = new ChooseImageController(ureq, getWindowControl(), false, entry);
		listenTo(chooseImageController);
		String title = translate("choose.image");
		cmc = new CloseableModalController(getWindowControl(), null,
				chooseImageController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doChangeLayout(UserRequest ureq) {
		QuizSettings quizSettings = getQuizSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(quizSettings);
		layoutTabComponents.sync(layoutSettings);
		quizSettings.setLayoutSettings(layoutSettings);

		quizElement.setSettings(quizSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		QuizSettings quizSettings = getQuizSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(quizSettings);
		alertBoxComponents.sync(alertBoxSettings);
		quizSettings.setAlertBoxSettings(alertBoxSettings);

		quizElement.setSettings(quizSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private BlockLayoutSettings getLayoutSettings(QuizSettings quizSettings) {
		if (quizSettings.getLayoutSettings() != null) {
			return quizSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(QuizSettings quizSettings) {
		if (quizSettings.getAlertBoxSettings() != null) {
			return quizSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private QuizSettings getQuizSettings() {
		if (quizElement.getSettings() != null) {
			return quizElement.getSettings();
		}
		return new QuizSettings();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
}
