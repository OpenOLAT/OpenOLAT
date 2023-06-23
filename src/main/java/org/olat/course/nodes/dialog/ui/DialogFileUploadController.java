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
package org.olat.course.nodes.dialog.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Controller for uploading dialog files
 * <p>
 * Initial date: Jun 08, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DialogFileUploadController extends FormBasicController {

	protected static final String DIALOG_ACTION_UPLOAD = "dialog.action.upload";
	protected static final String DIALOG_ACTION_COPY = "dialog.action.copy";

	private final UserCourseEnvironment userCourseEnv;
	private final boolean canCopyFile;
	protected FileElement fileUploadEl;
	private FormLayoutContainer searchCont;
	private FormLayoutContainer dialogMetadataCont;
	private FormLink selectFileLink;
	private SingleSelection actionSelection;
	private TextElement fileNameEl;
	private TextElement publishedByEl;
	private TextElement authoredByEl;
	private TextElement fileChooserEl;
	private FormSubmit submitBtn;
	private CloseableModalController cmc;
	private LinkChooserController fileCopyCtrl;


	public DialogFileUploadController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
									  boolean canCopyFile) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.canCopyFile = canCopyFile;
		this.userCourseEnv = userCourseEnv;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer dialogActionCont = uifactory.addDefaultFormLayout("def.dialog.actions", null, formLayout);

		SelectionValues actionsSV = new SelectionValues();
		actionsSV.add(entry(DIALOG_ACTION_UPLOAD, translate(DIALOG_ACTION_UPLOAD), translate("dialog.action.upload.desc"), "o_icon o_icon_upload", null, true));
		actionsSV.add(entry(DIALOG_ACTION_COPY, translate(DIALOG_ACTION_COPY), translate("dialog.action.copy.desc"), "o_icon o_icon_search", null, true));
		actionSelection = uifactory.addCardSingleSelectHorizontal("dialog.action.selection", "dialog.action.selection", dialogActionCont, actionsSV);
		actionSelection.select(DIALOG_ACTION_UPLOAD, true);
		actionSelection.addActionListener(FormEvent.ONCHANGE);

		fileUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "dialog.upload.file.element", dialogActionCont);
		fileUploadEl.addActionListener(FormEvent.ONCHANGE);
		fileUploadEl.setMaxUploadSizeKB((int) FolderConfig.getLimitULKB(), "attachments.too.big", new String[]{((Long) (FolderConfig
				.getLimitULKB() / 1024)).toString()});
		fileUploadEl.setMandatory(true, "dialog.selected.element.empty");

		searchCont = FormLayoutContainer.createInputGroupLayout("searchWrapper", getTranslator(), null, null);
		searchCont.setLabel("dialog.upload.file.element", null);
		searchCont.setMandatory(true);
		dialogActionCont.add("searchWrapper", searchCont);
		searchCont.setRootForm(mainForm);

		fileChooserEl = uifactory.addTextElement("fileChooser", "dialog.upload.file.element", 256, null, searchCont);
		fileChooserEl.setDomReplacementWrapperRequired(false);
		fileChooserEl.setEnabled(false);

		selectFileLink = uifactory.addFormLink("rightAddOn", "", "", searchCont, Link.NONTRANSLATED);
		selectFileLink.setCustomEnabledLinkCSS("o_link_plain");
		selectFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		String searchLabel = getTranslator().translate("dialog.select.file");
		selectFileLink.setLinkTitle(searchLabel);
		selectFileLink.setI18nKey(searchLabel);

		dialogMetadataCont = uifactory.addDefaultFormLayout("def.dialog.metadata", null, formLayout);
		fileNameEl = uifactory.addTextElement("filename", "dialog.metadata.filename", 256, null, dialogMetadataCont);
		fileNameEl.setMandatory(true);
		publishedByEl = uifactory.addInlineTextElement("publishedby", "dialog.metadata.published.by",
				ureq.getIdentity().getUser().getLastName() + ", " + ureq.getIdentity().getUser().getFirstName(),
				dialogMetadataCont, this);
		publishedByEl.setEnabled(false);
		authoredByEl = uifactory.addTextElement("authoredby", "dialog.metadata.authored.by", 256, null, dialogMetadataCont);


		// buttons
		FormLayoutContainer buttonLayoutCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		dialogMetadataCont.add(buttonLayoutCont);
		submitBtn = uifactory.addFormSubmitButton("dialog.upload.submit", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());

		updateVisibility();
	}

	private void updateVisibility() {
		fileUploadEl.setVisible(actionSelection.isKeySelected(DIALOG_ACTION_UPLOAD));
		searchCont.setVisible(actionSelection.isKeySelected(DIALOG_ACTION_COPY));
		actionSelection.setVisible(canCopyFile);

		boolean visibilityMetadata = fileUploadEl.isUploadSuccess() && fileUploadEl.isVisible()
				|| !fileChooserEl.isEmpty() && searchCont.isVisible();
		fileNameEl.setVisible(visibilityMetadata);
		publishedByEl.setVisible(visibilityMetadata);
		authoredByEl.setVisible(visibilityMetadata);
		submitBtn.setEnabled(visibilityMetadata);
		if (visibilityMetadata) {
			dialogMetadataCont.setFormTitle(translate("dialog.metadata.title"));
		} else {
			dialogMetadataCont.setFormTitle(null);
		}
	}

	private void doCopy(UserRequest ureq) {
		VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		fileCopyCtrl = new LinkChooserController(ureq, getWindowControl(), courseContainer, null, null, null, false, "", null, null, true);
		listenTo(fileCopyCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), fileCopyCtrl.getInitialComponent(),
				true, translate("dialog.copy.file"));
		listenTo(cmc);
		cmc.activate();
	}

	public String getActionSelectedKey() {
		return actionSelection.getSelectedKey();
	}

	public FileElement getFileUploadEl() {
		return fileUploadEl;
	}

	/**
	 * @return String value, retrieve filtered filename
	 */
	public String getFileNameElValue() {
		return StringHelper.escapeHtml(fileNameEl.getValue());
	}

	/**
	 * @return String value, retrieve filtered authoredBy name
	 */
	public String getAuthoredByElValue() {
		return StringHelper.escapeHtml(authoredByEl.getValue());
	}

	/**
	 * @return String value, retrieve filtered chosen filename
	 */
	public String getFileChooserElValue() {
		return fileChooserEl.getValue();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = super.validateFormLogic(ureq);
		String fileName = fileNameEl.getValue();

		if (!FileUtils.validateFilename(fileName)) {
			fileNameEl.setErrorKey("dialog.metadata.filename.error");
			isInputValid = false;
		}

		return isInputValid;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileCopyCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
			if (event instanceof URLChoosenEvent urlChoosenEvent) {
				String fileUrl = urlChoosenEvent.getURL();
				String[] pathElements = urlChoosenEvent.getURL().split("/");
				String fileName = pathElements[pathElements.length - 1];
				fileChooserEl.setValue(fileUrl);
				fileNameEl.setValue(fileName);
				cmc.deactivate();
				updateVisibility();
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == actionSelection) {
			updateVisibility();
		} else if (source == selectFileLink) {
			doCopy(ureq);
		} else if (source == fileUploadEl) {
			fileNameEl.setValue(fileUploadEl.getUploadFileName());
			updateVisibility();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
