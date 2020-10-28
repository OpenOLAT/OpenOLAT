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

package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Description:<br>
 * This class implements the component used by the rich text form element based
 * on the TinyMCE javascript library.
 * 
 * 
 * <P>
 * Initial Date: 21.04.2009 <br>
 * 
 * @author gnaegi
 */
class RichTextElementComponent extends FormBaseComponentImpl implements ControllerEventListener {
	private static final String CMD_IMAGEBROWSER = "image";
	private static final String CMD_FLASHPLAYERBROWSER = "flashplayer";
	private static final String CMD_FILEBROWSER = "file";
	private static final String CMD_MEDIABROWSER = "media";
	private static final ComponentRenderer RENDERER = new RichTextElementRenderer();

	private final RichTextElementImpl element;
	private int cols;
	private int rows;
	private Integer currentHeight;
	private TextMode currentTextMode;
	
	private CloseableModalController cmc;
	private LinkChooserController myLinkChooserController;

	/**
	 * Constructor for a text area element
	 * 
	 * @param element
	 * @param rows
	 *            the number of lines or -1 to use default value
	 * @param cols
	 *            the number of characters per line or -1 to use 100% of the
	 *            available space
	 */
	public RichTextElementComponent(RichTextElementImpl element, int rows,
			int cols) {
		super(element.getName());
		this.element = element;
		setCols(cols);
		setRows(rows);
	}

	RichTextElementImpl getRichTextElementImpl() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	protected Integer getCurrentHeight() {
		return currentHeight;
	}

	protected void setCurrentHeight(Integer currentHeight) {
		this.currentHeight = currentHeight;
	}

	protected TextMode getCurrentTextMode() {
		return currentTextMode;
	}

	protected void setCurrentTextMode(TextMode currentTextMode) {
		this.currentTextMode = currentTextMode;
		setDirty(true);
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/tinymce4/BTinyHelper.js");
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// SPECIAL CASE - normally this method is never overriden. For the rich text
		// element we make an exception since we have the media and link chooser
		// events that must be dispatched by this code.		
		String moduleUri = ureq.getModuleURI();
		if (CMD_FILEBROWSER.equals(moduleUri) || CMD_IMAGEBROWSER.equals(moduleUri)
				|| CMD_FLASHPLAYERBROWSER.equals(moduleUri) || CMD_MEDIABROWSER.equals(moduleUri)) {
			// Get currently edited relative file path
			String fileName = getRichTextElementImpl().getEditorConfiguration().getLinkBrowserRelativeFilePath();
			createFileSelectorPopupWindow(ureq, moduleUri, fileName);
			setDirty(false);
		} else if(StringHelper.containsNonWhitespace(ureq.getParameter("browser"))) {
			String fileName = getRichTextElementImpl().getEditorConfiguration().getLinkBrowserRelativeFilePath();
			createFileSelectorPopupWindow(ureq, ureq.getParameter("browser"), fileName);
		} else {
			String cmd = ureq.getParameter("cmd");
			if(StringHelper.containsNonWhitespace(cmd)) {
				element.getRootForm().fireFormEvent(ureq, new FormEvent(cmd, element, FormEvent.ONCLICK));
			}
			setDirty(false);
		}
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanUp();
		} else if(source == myLinkChooserController) {
			if(event instanceof URLChoosenEvent) {
				doSendUrlToTiny(ureq, (URLChoosenEvent)event);
			}
			cmc.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		myLinkChooserController.removeControllerListener(this);
		cmc.removeControllerListener(this);
		myLinkChooserController = null;
		cmc = null;
	}
	
	private void doSendUrlToTiny(UserRequest ureq, URLChoosenEvent urlChoosenEvent) {
		String escapedUrl;
		String url = urlChoosenEvent.getURL();
		if (url.contains("gotonode") || url.contains("gototool")) {
			escapedUrl = url;
		} else if(url.startsWith("http://") || url.startsWith("https://")) {
			escapedUrl = url;
		} else {
			escapedUrl = escapeUrl(url);
		}
		
		StringBuilder cmd = new StringBuilder();
		cmd.append("BTinyHelper.writeLinkSelectionToTiny('").append(escapedUrl).append("'");
		if(urlChoosenEvent.getWidth() > 0) {
			cmd.append(",").append(urlChoosenEvent.getWidth());
		}
		if(urlChoosenEvent.getHeight() > 0) {
			cmd.append(",").append(urlChoosenEvent.getHeight());
		}
		cmd.append(");");
		
		JSCommand writeLinkSelectionToTiny = new JSCommand(cmd.toString());
		 Windows.getWindows(ureq).getWindow(ureq).getWindowBackOffice().sendCommandTo(writeLinkSelectionToTiny);
	}
	
	private String escapeUrl(String url) {
		return url.replace("+", "%2b");
	}

	private void createFileSelectorPopupWindow(final UserRequest ureq, final String type, final String fileName) {
		if(myLinkChooserController != null) return;
		
		// Get allowed suffixes from configuration and requested media browser type from event
		final RichTextConfiguration config = element.getEditorConfiguration();
		final boolean allowCustomMediaFactory = config.isAllowCustomMediaFactory();
		final boolean uriValidation = config.isFilenameUriValidation();
		final String[] suffixes;
		if(type.equals(CMD_FILEBROWSER)) {
			suffixes = null;
		} else if(type.equals(CMD_IMAGEBROWSER)) {
			suffixes = config.getLinkBrowserImageSuffixes();
		} else if (type.equals(CMD_FLASHPLAYERBROWSER)) {
			suffixes = config.getLinkBrowserFlashPlayerSuffixes();
		} else {
			suffixes = config.getLinkBrowserMediaSuffixes();
		}
		
		// Show popup window with file browser to select file
		// Only one popup file chooser allowed at any time. ccc contains icc,
		// icc gets disposed by ccc
		
		//helper code which is used to create link chooser controller
		
		WindowControl wControl = Windows.getWindows(ureq).getWindow(ureq).getWindowBackOffice().getChiefController().getWindowControl();

		VFSContainer baseContainer = config.getLinkBrowserBaseContainer();
		String uploadRelPath = config.getLinkBrowserUploadRelPath();
		String absolutePath = config.getLinkBrowserAbsolutFilePath();
		CustomLinkTreeModel linkBrowserCustomTreeModel = config.getLinkBrowserCustomLinkTreeModel();
		CustomLinkTreeModel toolLinkTreeModel = config.getToolLinkTreeModel();
		if (type.equals(CMD_FILEBROWSER)) {
			// when in file mode we include the internal links to the selection
			myLinkChooserController = new LinkChooserController(ureq, wControl, baseContainer, uploadRelPath, absolutePath, suffixes, uriValidation, fileName, linkBrowserCustomTreeModel, toolLinkTreeModel, allowCustomMediaFactory);			
		} else {
			// in media or image mode, internal links make no sense here
			myLinkChooserController = new LinkChooserController(ureq, wControl, baseContainer, uploadRelPath, absolutePath, suffixes, uriValidation, fileName, null, null, allowCustomMediaFactory);						
		}
		myLinkChooserController.addControllerListener(this);

		cmc = new CloseableModalController(wControl, "close", myLinkChooserController.getInitialComponent(), true, myLinkChooserController.getTitle());
		cmc.suppressDirtyFormWarning();
		cmc.topModal();
		cmc.activate();
		cmc.addControllerListener(this);
	}
}