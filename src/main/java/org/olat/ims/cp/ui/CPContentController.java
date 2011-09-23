/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.ims.cp.ui;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.CPManagerImpl;
import org.olat.ims.cp.ContentPackage;
import org.olat.modules.cp.CPUIFactory;

public class CPContentController extends BasicController {

	private static final String FILE_SUFFIX_HTM = "htm";
	private IFrameDisplayController iframeCtr;
	private HTMLEditorController mceCtr; // WYSIWYG
	private VelocityContainer contentVC;
	private ContentPackage cp;
	private CPPage currentPage;
	private CPMetadataEditController editMetadataCtr;
	private CloseableModalController dialogCtr;
	private LayoutMain3ColsPreviewController previewCtr;
	private Link editMetadataLink, previewLink;

	protected CPContentController(UserRequest ureq, WindowControl control, ContentPackage cp) {
		super(ureq, control);

		this.cp = cp;

		contentVC = createVelocityContainer("cpContent");
		// set initial page to display

		iframeCtr = new IFrameDisplayController(ureq, control, cp.getRootDir());
		listenTo(iframeCtr);

	}

	protected void init(UserRequest ureq) {
		editMetadataLink = LinkFactory.createCustomLink("contentcontroller.editlink", "contentcontroller.editlink", null, Link.NONTRANSLATED,
				contentVC, this);
		editMetadataLink.setCustomEnabledLinkCSS("o_cpeditor_edit");
		editMetadataLink.setTooltip(translate("contentcontroller.editlink_title"), false);

		previewLink = LinkFactory.createCustomLink("contentcontroller.previewlink", "contentcontroller.previewlink", null, Link.NONTRANSLATED,
				contentVC, this);
		previewLink.setCustomEnabledLinkCSS("o_cpeditor_preview");
		previewLink.setTooltip(translate("contentcontroller.previewlink_title"), false);

		this.putInitialPanel(contentVC);

		CPManagerImpl cpMgm = (CPManagerImpl) CPManager.getInstance();
		currentPage = cpMgm.getFirstPageToDisplay(cp);
		displayPage(ureq, currentPage.getIdentifier());
	}

	/**
	 * Displays the correct edit page when node with the given id is selected.
	 * 
	 * @param nodeID
	 */
	protected void displayPage(UserRequest ureq, String nodeID) {
		CPManagerImpl cpMgm = (CPManagerImpl) CPManager.getInstance();

		currentPage = new CPPage(nodeID, cp);

		String filePath = cpMgm.getPageByItemId(cp, currentPage.getIdentifier());
		logInfo("I display the page with id: " + currentPage.getIdentifier(), null);

		VFSItem f = cp.getRootDir().resolve(filePath);
		if (filePath == null) {
			displayInfoPage();

		} else if (f == null) {
			displayNotFoundPage(filePath);

		} else {
			currentPage.setFile((VFSLeaf) f);
			setContent(ureq, filePath);
		}
		fireEvent(ureq, new Event("Page loaded"));
	}

	/**
	 * Set the content to display given the file path
	 * 
	 * @param ureq
	 * @param filePath
	 */
	private void setContent(UserRequest ureq, String filePath) {
		if (filePath.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (filePath.length() - 4)) {
			if (mceCtr != null) mceCtr.dispose();
			mceCtr = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), currentPage.getRootDir(), filePath, false);
			mceCtr.setCancelButtonEnabled(false);
			mceCtr.setSaveCloseButtonEnabled(false);
			mceCtr.setShowMetadataEnabled(false);
			listenTo(mceCtr);
			contentVC.put("content", mceCtr.getInitialComponent());
		} else {
			iframeCtr.setCurrentURI(filePath);
			contentVC.put("content", iframeCtr.getInitialComponent());
		}
	}

	/**
	 * displays a info page in the "content-area" of the cpEditor
	 * 
	 * see: ../_content/infoPage.html
	 * 
	 */
	protected void displayInfoPage() {
		if (currentPage != null) currentPage.setFile(null);
		VelocityContainer infoVC = createVelocityContainer("infoPage");
		infoVC.contextPut("infoChapterpage", translate("contentcontroller.infoChapterpage"));
		contentVC.put("content", infoVC);
	}

	/**
	 * displays a info page in the "content-area" of the cpEditor
	 * 
	 * see: ../_content/infoPage.html
	 * 
	 */
	protected void displayNotFoundPage(String requestedPage) {
		currentPage.setFile(null);
		VelocityContainer nfVC = createVelocityContainer("notFoundPage");
		// Don't display the file name. It's too much information.
		nfVC.contextPut("not_found_message", translate("contentcontroller.page.not.found"));
		contentVC.put("content", nfVC);
	}

	/**
	 * Displays the editPageEditor
	 * 
	 * @param ureq
	 */
	private void displayMetadataEditor(UserRequest ureq) {
		editMetadataCtr = new CPMetadataEditController(ureq, getWindowControl(), currentPage, cp);
		listenTo(editMetadataCtr);
		dialogCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), editMetadataCtr.getInitialComponent());
		listenTo(dialogCtr);
		dialogCtr.activate();
	}

	/**
	 * @return The current page
	 */
	protected CPPage getCurrentPage() {
		return currentPage;
	}

	/**
	 * this function is used to return the new nodeID of the just added page back
	 * to the pageEditController
	 */
	protected void newPageAdded(String newNodeID) {
		editMetadataCtr.newPageAdded(newNodeID);
	}

	@Override
	protected void doDispose() {
	// Nothing to implement since this controller listens to iframeCtr and
	// dialogCtr.
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editMetadataLink) {
			displayMetadataEditor(ureq);
		} else if (source == previewLink) {
			displayPreview(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editMetadataCtr) {
			// event from editPage controller, such as "Save", "Save and Close",
			// "Cancel"
			if (event.equals(Event.CANCELLED_EVENT)) {
				dialogCtr.deactivate();
			} else if (event.equals(Event.DONE_EVENT)) {
				// close and save
				dialogCtr.deactivate();
				fireEvent(ureq, new NewCPPageEvent("Page Saved", editMetadataCtr.getCurrentPage()));

			} else if (event.getCommand().equals("saved")) {
				// save but do not close
				fireEvent(ureq, new NewCPPageEvent("Page Saved", editMetadataCtr.getCurrentPage()));
			}
		} else if (source == dialogCtr) {
			if (event.getCommand().equals("CLOSE_MODAL_EVENT")) {
				// close (x) button clicked in modal dialog
				// System.out.println("modal dialog closed (x)");
			}
		} else if (source == mceCtr) {
			if (event.getCommand().equals("CLOSE_MODAL_EVENT")) {
				// close (x) button clicked in modal dialog
				// System.out.println("modal dialog closed (x)");
			}
		} else if (source == iframeCtr) {
			if (event instanceof NewIframeUriEvent) {
				// html link clicked in content (iframe)
				fireEvent(ureq, event);
			}
		}

	}

	/**
	 * Displays the preview
	 * 
	 * @param ureq
	 */
	private void displayPreview(UserRequest ureq) {
		if (previewCtr != null) previewCtr.dispose();
		previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController(ureq, getWindowControl(), cp.getRootDir(), true);
		previewCtr.activate();
	}

}
