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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.ims.cp.ui;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ContentPackage;
import org.olat.modules.cp.CPUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CPContentController extends BasicController {

	private static final String FILE_SUFFIX_HTM = "htm";
	private IFrameDisplayController iframeCtr;
	private HTMLEditorController mceCtr; // WYSIWYG
	private ContentPackage cp;
	private CPPage currentPage;
	private CPMetadataEditController editMetadataCtr;
	private CloseableModalController dialogCtr;
	private LayoutMain3ColsPreviewController previewCtr;
	private Link editMetadataLink, previewLink;
	private Component helpLink;
	private DeliveryOptions deliveryOptions;
	private StackedPanel mainPanel;
	
	@Autowired
	private CPManager cpManager;
	@Autowired
	private HelpModule helpModule;

	protected CPContentController(UserRequest ureq, WindowControl control, ContentPackage cp) {
		super(ureq, control);

		this.cp = cp;
		
		CPPackageConfig packageConfig = cpManager.getCPPackageConfig(cp.getResourcable());
		if(packageConfig != null) {
			deliveryOptions = packageConfig.getDeliveryOptions();
		}

		// init help link, can't do this in initToolbar because ureq is missing
		if (helpModule.isHelpEnabled()) {
			HelpLinkSPI provider = helpModule.getManualProvider();
			helpLink = provider.getHelpPageLink(ureq, translate("help"), translate("helpbutton"), "o_icon o_icon-lg o_icon_help", null, "CP Editor");
		}
		
		// set initial page to display
		iframeCtr = new IFrameDisplayController(ureq, control, cp.getRootDir());
		listenTo(iframeCtr);
	}

	protected void init(UserRequest ureq) {
		mainPanel = putInitialPanel(new SimpleStackedPanel("cpContent"));

		currentPage = cpManager.getFirstPageToDisplay(cp);
		displayPage(ureq, currentPage.getIdentifier());
	}
	
	void initToolbar(TooledStackedPanel toolbar) {
		editMetadataLink = LinkFactory.createToolLink("contentcontroller.editlink", "contentcontroller.editlink",
				translate("contentcontroller.editlink_title"), this);
		editMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
		editMetadataLink.setElementCssClass("o_sel_cp_edit_metadata");
		editMetadataLink.setTitle(translate("contentcontroller.editlink_title"));
		toolbar.addTool(editMetadataLink, Align.right);

		previewLink = LinkFactory.createToolLink("contentcontroller.previewlink", "contentcontroller.previewlink",
				translate("contentcontroller.previewlink_title"), this);
		previewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_preview");
		previewLink.setElementCssClass("o_sel_cp_preview");
		previewLink.setTitle(translate("contentcontroller.previewlink_title"));
		toolbar.addTool(previewLink, Align.right);

		if (helpLink != null) {
			toolbar.addTool(helpLink, Align.right);			
		}
	}

	/**
	 * Displays the correct edit page when node with the given id is selected.
	 * 
	 * @param ureq
	 * @param nodeID
	 */
	protected void displayPage(UserRequest ureq, String nodeID) {
		currentPage = new CPPage(nodeID, cp);

		String filePath = cpManager.getPageByItemId(cp, currentPage.getIdentifier());
		logInfo("I display the page with id: " + currentPage.getIdentifier());

		VFSItem f = cp.getRootDir().resolve(filePath);
		if (filePath == null) {
			displayInfoPage();
		} else if (f == null) {
			displayNotFoundPage();
		} else {
			currentPage.setFile((VFSLeaf) f);
			setContent(ureq, filePath);
		}
		fireEvent(ureq, new Event("Page loaded"));
	}
	
	/**
	 * Displays the page editor and shows the metadata editor to rename the page
	 * @param ureq
	 * @param nodeID
	 */
	protected void displayPageWithMetadataEditor(UserRequest ureq, String nodeID) {
		displayPage(ureq, nodeID);
		displayMetadataEditor(ureq);
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
			
			VFSContainer rootDir = currentPage.getRootDir();
			String virtualRootFolderName = translate("cpfileuploadcontroller.virtual.root");
			VFSContainer pseudoContainer = new VFSRootCPContainer(virtualRootFolderName, cp, rootDir, getTranslator());

			mceCtr = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), pseudoContainer, filePath, false, false);
			if(mceCtr.isEditable()) {
				mceCtr.setCancelButtonEnabled(false);
				mceCtr.setSaveCloseButtonEnabled(false);
				mceCtr.setShowMetadataEnabled(false);
			}
			listenTo(mceCtr);
			mainPanel.setContent(mceCtr.getInitialComponent());
		} else {
			iframeCtr.setCurrentURI(filePath);
			mainPanel.setContent(iframeCtr.getInitialComponent());
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
		mainPanel.setContent(infoVC);
	}

	/**
	 * displays a info page in the "content-area" of the cpEditor
	 * 
	 * see: ../_content/infoPage.html
	 * 
	 */
	protected void displayNotFoundPage() {
		currentPage.setFile(null);
		VelocityContainer nfVC = createVelocityContainer("notFoundPage");
		// Don't display the file name. It's too much information.
		nfVC.contextPut("not_found_message", translate("contentcontroller.page.not.found"));
		mainPanel.setContent(nfVC);
	}

	/**
	 * Displays the editPageEditor
	 * 
	 * @param ureq
	 */
	private void displayMetadataEditor(UserRequest ureq) {
		editMetadataCtr = new CPMetadataEditController(ureq, getWindowControl(), currentPage);
		listenTo(editMetadataCtr);
		String title = translate("cpmd.flexi.formtitle");
		dialogCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"),
				editMetadataCtr.getInitialComponent(), true, title);
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
				fireEvent(ureq, new NewCPPageEvent("Page Saved", editMetadataCtr.getPage()));

			} else if (event.getCommand().equals("saved")) {
				// save but do not close
				fireEvent(ureq, new NewCPPageEvent("Page Saved", editMetadataCtr.getPage()));
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
		previewCtr = CPUIFactory.getInstance().createMainLayoutPreviewController(ureq, getWindowControl(), cp.getRootDir(), true, deliveryOptions);
		previewCtr.activate();
	}

}
