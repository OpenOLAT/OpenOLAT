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
package org.olat.core.commons.controllers.filechooser;

import java.util.regex.Pattern;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * Description:
 * <p>
 * This controller shows a form where the user can edit the filename of a new
 * file and the optional sub-path under which the file is created.
 * <p />
 * Fires Event.DONE_EVENT when finished and Event.CANCELLED_EVENT otherwise.
 * When done, the created file can be retrieved with the getCreatedFile()
 * method.
 * <p />
 * 
 * Initial date: 15.01.2015<br>
 * 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class FileCreatorController extends FormBasicController {

	private final VFSContainer baseContainer;
	private final String subfolderPath;
	private final String fileName;
	private VFSLeaf createdFile;

	private TextElement fileNameElement;
	private TextElement targetSubPath;
	
	private static final Pattern validSubPathPattern = Pattern.compile("[\\p{Alnum}-_\\./]*");

	/**
	 * Create a file creator instance
	 * 
	 * @param ureq
	 *            User request
	 * @param wControl
	 *            Window control
	 * @param rootContainer
	 *            The root container in which the file can be created
	 * @param subfolderPath
	 *            A subfolder path that should be prefilled, can be NULL
	 * @param fileName 
	 */
	public FileCreatorController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String subfolderPath, String fileName) {
		super(ureq, wControl, Util.createPackageTranslator(FileUploadController.class, ureq.getLocale()));
		this.baseContainer = rootContainer;
		this.subfolderPath = subfolderPath;
		this.fileName = fileName;
		initForm(ureq);	
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {										
		setFormTitle("filecreator.text.newfile");
		formLayout.setElementCssClass("o_sel_file_form");
		
		// Global target directory
		String path = "/ " + baseContainer.getName();
		VFSContainer container = baseContainer.getParentContainer();
		while (container != null) {
			path = "/ " + container.getName() + " " + path;
			container = container.getParentContainer();
		}
		uifactory.addStaticTextElement("ul.target", path,formLayout);

		// Sub path, can be modified
		targetSubPath = uifactory.addInlineTextElement("ul.target.child", subfolderPath, formLayout, this);	
		targetSubPath.setLabel("ul.target.child", null);

		// The file name of the new file
		fileNameElement = FormUIFactory.getInstance().addTextElement("fileName", "filecreator.filename", 50, fileName, formLayout);
		fileNameElement.setPlaceholderKey("filecreator.filename.placeholder", null);
		fileNameElement.setElementCssClass("o_sel_filename");
		fileNameElement.setMandatory(true);

		// Add buttons
		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttons);
		uifactory.addFormSubmitButton("submit", "button.create", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());			

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isFileNmaeValid = true;
		boolean isSubDirValid = true;
		// 1: Check sub path
		String subPath = targetSubPath.getValue();
		if (subPath != null) {
			// Cleanup first
			subPath = subPath.toLowerCase().trim();
			if (!validSubPathPattern.matcher(subPath).matches()) {
				targetSubPath.setErrorKey("subpath.error.characters", null);
				isSubDirValid = false;
			} else {
				// Fix mess with slashes and dots
				// reduce doubled slashes with single slash
				subPath = subPath.replaceAll("\\.*\\/+\\.*", "\\/");
				// do it a second time to catch the double slashes created by previous replacement
				subPath = subPath.replaceAll("\\/+", "\\/");
				// remove slash at end
				if (subPath.endsWith("/")) {
					subPath = subPath.substring(0, subPath.length()-1);
				}
				// single slash means no sub-directory
				if (subPath.length() == 1 && subPath.startsWith("/")) {
					subPath = "";
				}				
				// fix missing slash at start
				if (subPath.length() > 0 && !subPath.startsWith("/")) {
					subPath = "/" + subPath;
				}
				// update in GUI so user sees how we optimized
				targetSubPath.setValue(subPath);
			}
			// Now check if this path does not already exist
			if (isSubDirValid && StringHelper.containsNonWhitespace(subPath)){
				// Try to resolve given rel path from current container
				VFSItem uploadDir = baseContainer.resolve(subPath);
				if (uploadDir != null) {
					// already exists. this is fine, as long as it is a directory and not a file
					if (!(uploadDir instanceof VFSContainer)) {
						// error
						targetSubPath.setErrorKey("subpath.error.dir.is.file", new String[] {subPath});
						isSubDirValid = false;
					}
				}
			}
			
			if (isSubDirValid) {
				targetSubPath.clearError();
			}
		}
		

		// 2: Check file name
		String fileName = fileNameElement.getValue();		
		if(!StringHelper.containsNonWhitespace(fileName)) {
			fileNameElement.setErrorKey("mf.error.filename.empty", new String[0]);
			isFileNmaeValid = false;
		} else {
			fileName = fileName.toLowerCase().trim();
			if (!FileUtils.validateFilename(fileName)) {
				fileNameElement.setErrorKey("mf.error.filename.invalidchars", new String[0]);
				isFileNmaeValid = false;
			} else if (!fileName.endsWith(".html") && !fileName.endsWith(".htm")) {
				fileName = fileName + ".html";
			}
			// update in GUI so user sees how we optimized
			fileNameElement.setValue(fileName);
			// check if it already exists
			String filePath = fileName;
			if (filePath.startsWith("/")) {
				filePath = "/" + filePath;
			}
			if (StringHelper.containsNonWhitespace(targetSubPath.getValue())) {
				filePath = targetSubPath.getValue() + "/" + filePath;
			}
			VFSItem vfsItem = baseContainer.resolve(filePath);
			if (vfsItem != null) {
				fileNameElement.setErrorKey("mf.error.filename.exists", new String[] {filePath});
				isFileNmaeValid = false;
			}
		}
		if (isFileNmaeValid) {
			fileNameElement.clearError();
		}
		return isFileNmaeValid && isSubDirValid;			
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// 1: Get parent container for new file
		String uploadRelPath = targetSubPath.getValue();
		VFSContainer parentContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, uploadRelPath);
		if (parentContainer == null) {
			logError("Can not create target sub path::" + uploadRelPath + ", fall back to base container", null);
			parentContainer = baseContainer;
		}

		// 2: Create empty file in parent
		String fileName = fileNameElement.getValue();
		VFSItem resolvedFile = parentContainer.resolve(fileName);
		if (resolvedFile == null) {
			createdFile = parentContainer.createChildLeaf(fileName);			
		} else {
			createdFile = (VFSLeaf) resolvedFile;
		}
		
		// Let others know that we have a great new (empty) file created ready
		// for anything you want to do with it
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);      
		super.formCancelled(ureq);
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		fileNameElement.reset();
		targetSubPath.reset();
	}	

	/**
	 * Get the created file or NULL if no file has been created
	 * @return
	 */
	public VFSLeaf getCreatedFile(){
		return this.createdFile;
	}
}
