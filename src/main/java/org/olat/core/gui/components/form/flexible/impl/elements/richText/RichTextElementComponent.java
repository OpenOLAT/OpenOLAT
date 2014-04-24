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
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.helpers.Settings;
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
class RichTextElementComponent extends FormBaseComponentImpl {
	private static final String CMD_IMAGEBROWSER = "image";
	private static final String CMD_FLASHPLAYERBROWSER = "flashplayer";
	private static final String CMD_FILEBROWSER = "file";

	private ComponentRenderer RENDERER = new RichTextElementRenderer();
	private RichTextElementImpl element;
	private int cols;
	private int rows;
	
	private boolean useTiny4 = true;

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

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
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

	public boolean isUseTiny4() {
		return useTiny4;
	}

	/**
	 * @see org.olat.core.gui.components.Component#validate(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.render.ValidationResult)
	 */
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		
		if(useTiny4) {
			jsa.addRequiredStaticJsFile("js/tinymce4/BTinyHelper.js");
		} else {
			// Add tiny helper library
			//jsa.addRequiredJsFile(RichTextElementComponent.class,"js/BTinyHelper.js", "UTF-8");
			jsa.addRequiredStaticJsFile("js/tinymce/BTinyHelper.js");
	
			// When the tiny_mce.js is inserted via AJAX, we need to setup some
			// variables first to make it load properly:
			StringOutput sb = new StringOutput();
			// 1) Use tinyMCEPreInit to prevent TinyMCE to guess the script URL. The
			// script URL is needed because TinyMCE will load CSS, plugins and other
			// resources
			sb.append("tinyMCEPreInit = {};")
			  .append("tinyMCEPreInit.suffix = '';")
			  .append("tinyMCEPreInit.base = '");
			StaticMediaDispatcher.renderStaticURI(sb, "js/tinymce/tinymce", false);
			sb.append("';");
	
			// 2) Tell TinyMCE that the page has already been loaded
			sb.append("tinyMCE_GZ = {};");
			sb.append("tinyMCE_GZ.loaded = true;");
			String preAJAXinsertionCode = sb.toString();
	
			// Now add tiny library itself. TinyMCE files are written in iso-8859-1
			// (important, IE panics otherwise with error 8002010)
			if (Settings.isDebuging()) {
				jsa.addRequiredStaticJsFile("js/tinymce/tinymce/tiny_mce_src.js", "ISO-8859-1",preAJAXinsertionCode);
			} else {
				jsa.addRequiredStaticJsFile("js/tinymce/tinymce/tiny_mce.js", "ISO-8859-1", preAJAXinsertionCode);
			}
		}
	}

	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// SPECIAL CASE - normally this method is never overriden. For the rich text
		// element we make an exception since we have the media and link chooser
		// events that must be dispatched by this code.		
		String moduleUri = ureq.getModuleURI();
		if (moduleUri != null) {
			// Get currently edited relative file path
			String fileName = getRichTextElementImpl().getEditorConfiguration().getLinkBrowserRelativeFilePath();
			createFileSelectorPopupWindow(ureq, moduleUri, fileName);
		}
		setDirty(false);
	}
	
	
	private void createFileSelectorPopupWindow(final UserRequest ureq, final String type, final String fileName) {
		// Get allowed suffixes from configuration and requested media browser type from event
		final RichTextConfiguration config = element.getEditorConfiguration();
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
		ControllerCreator linkChooserControllerCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq,WindowControl lwControl) {
				LinkChooserController myLinkChooserController;
				VFSContainer baseContainer = config.getLinkBrowserBaseContainer();
				String uploadRelPath = config.getLinkBrowserUploadRelPath();
				CustomLinkTreeModel linkBrowserCustomTreeModel = config.getLinkBrowserCustomLinkTreeModel();
				if (type.equals(CMD_FILEBROWSER)) {
					// when in file mode we include the internal links to the selection
					//FIXME: user activity logger
					myLinkChooserController = new LinkChooserController(lureq, lwControl, baseContainer, uploadRelPath, suffixes, fileName, linkBrowserCustomTreeModel);			
				} else {
					// in media or image mode, internal links make no sense here
					myLinkChooserController = new LinkChooserController(lureq, lwControl, baseContainer, uploadRelPath, suffixes, fileName, null);						
				}
				return new LayoutMain3ColsController(lureq, lwControl, myLinkChooserController);
			}
		};
		PopupBrowserWindow pbw = Windows.getWindows(ureq).getWindowManager().createNewPopupBrowserWindowFor(ureq, linkChooserControllerCreator);
		pbw.open(ureq);
	}
}