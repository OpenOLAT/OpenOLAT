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
*/

package org.olat.commons.file.filechooser;

import java.io.File;

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserController;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.modules.bc.commands.CmdUpload;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFileTypeFilter;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.util.ContainerAndFile;
import org.olat.core.util.vfs.util.VFSUtil;

/**
 * Description: <br>
 * Use the setIframeEnabled for configuration of preview behaviour
 * 
 * @author alex
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FileChooseCreateEditController extends BasicController{
		
	private OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String ACTION_CHANGE 			= "changefile";

	private static final String VC_ENABLEEDIT 			= "enableEdit";
	private static final String VC_ENABLEDELETE 		= "enableDelete";
	private static final String VC_FILE_IS_CHOSEN		= "fileIsChoosen";
	private static final String VC_CHANGE 					= "fileHasChanged";
	private static final String VC_CHOSENFILE 			= "chosenFile";
	private static final String VC_FIELDSETLEGEND 	= "fieldSetLegend";
	
	// NLS support
	
	private static final String NLS_UNZIP_ALREADYEXISTS 					= "unzip.alreadyexists";
	private static final String NLS_FOLDER_DISPLAYNAME 						= "folder.displayname";
	private static final String NLS_ERROR_CHOOSEFILEFIRST 				= "error.choosefilefirst";
	private static final String NLS_ERROR_FILEDOESNOTEXIST 				= "error.filedoesnotexist";
	private static final String NLS_NO_FILE_CHOSEN 								= "no.file.chosen";
	private static final String NLS_ERROR_FILETYPE 								= "error.filetype";
	private static final String NLS_QUOTAEXEEDED									= "QuotaExceeded";
	
	private VelocityContainer myContent;
	private VelocityContainer fileChooser;
	
	private NewFileForm newFileForm;
	private AllowRelativeLinksForm allowRelativeLinksForm;
	
	private FileChooserController fileChooserCtr;
	private String chosenFile;
	private VFSContainer rootContainer;
	private boolean allowRelativeLinks;
	
	private CloseableModalController cmcFileChooser;
	private CloseableModalController cmcSelectionTree;
	private CloseableModalController cmcWysiwygCtr;
	private CmdUpload cmdUpload;
	private Controller wysiwygCtr;
	private LayoutMain3ColsPreviewController previewLayoutCtr;
	
	private boolean fileChooserActive = false;
	
	/** Event fired when another file has been choosen (filename has changed) **/
	public static final Event FILE_CHANGED_EVENT = new Event("filechanged");
	/** Event fired when the content of the file has been changed with the editor **/
	public static final Event FILE_CONTENT_CHANGED_EVENT = new Event("filecontentchanged");
	/** Event fired when configuration option to allow relative links has been changed **/
	public static final Event ALLOW_RELATIVE_LINKS_CHANGED_EVENT = new Event("allowrelativelinkschanged");
	public static final Event DELIVERY_OPTIONS_CHANGED_EVENT = new Event("deliveryoptionschanged");
	private Link editButton;
	private Link deleteButton;
	private Link changeFileButtonOne;
	private Link changeFileButtonTwo;
	private Link previewLink;
	private Link chooseFileButton;
	
	public static String[] INITIAL_ALLOWED_FILE_SUFFIXES = new String[] { "html", "htm", "xml", "xhtml" };
	private String[] allowedFileSuffixes = INITIAL_ALLOWED_FILE_SUFFIXES;
	
	private boolean allFileSuffixesAllowed = false;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param chosenFile
	 * @param allowRelativeLinks
	 * @param rootContainer
	 * @param target
	 * @param fieldSetLegend
	 */
	FileChooseCreateEditController(UserRequest ureq, WindowControl wControl, String chosenFile, Boolean allowRelativeLinks, VFSContainer rootContainer, String target, String fieldSetLegend ) {
		// use folder module fallback translator
		super(ureq,wControl, Util.createPackageTranslator(FolderModule.class, ureq.getLocale()));
		init(chosenFile, allowRelativeLinks, rootContainer, target, fieldSetLegend, ureq, wControl);
	}	

	private void init(String file, Boolean allowRelLinks, VFSContainer rContainer, String target, String fieldSetLegend, UserRequest ureq, WindowControl wControl )	{		
		if(log.isDebug()) {
			log.debug("Constructing FileChooseCreateEditController using the current velocity root");
		}
		
		this.chosenFile = file;		
		this.rootContainer = rContainer;
		this.allowRelativeLinks = allowRelLinks == null ? false : allowRelLinks.booleanValue();
		myContent = createVelocityContainer("chosenfile");
		editButton = LinkFactory.createButtonSmall("command.edit", myContent, this);
		editButton.setElementCssClass("o_sel_filechooser_edit");
		deleteButton = LinkFactory.createButtonSmall("command.delete", myContent, this);
		deleteButton.setElementCssClass("o_sel_filechooser_delete");
		changeFileButtonOne = LinkFactory.createButtonSmall("command.changefile", myContent, this);
		changeFileButtonOne.setElementCssClass("o_sel_filechooser_change");
		changeFileButtonTwo = LinkFactory.createButtonSmall("command.choosecreatefile", myContent, this);
		changeFileButtonTwo.setElementCssClass("o_sel_filechooser_create");
		previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", getTranslator().translate(NLS_FOLDER_DISPLAYNAME) + chosenFile, Link.NONTRANSLATED, myContent, this);
		previewLink.setElementCssClass("o_sel_filechooser_preview");
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		previewLink.setCustomEnabledLinkCSS("o_preview");
		previewLink.setTitle(getTranslator().translate("command.preview"));
		
		this.fileChooser = createVelocityContainer("filechoosecreateedit");
		chooseFileButton = LinkFactory.createButtonSmall("command.choosefile", fileChooser, this);
		
		fileChooser.contextPut(VC_FIELDSETLEGEND, fieldSetLegend);
		myContent.contextPut(VC_FIELDSETLEGEND, fieldSetLegend);
		fileChooser.contextPut("target", target);
		myContent.contextPut("target", target);
				
		newFileForm = new NewFileForm(ureq, wControl, getTranslator(), rootContainer);		
		listenTo(newFileForm);
		fileChooser.put("newfileform", newFileForm.getInitialComponent());
		
		allowRelativeLinksForm = new AllowRelativeLinksForm(ureq, wControl, allowRelativeLinks);
		listenTo(allowRelativeLinksForm);
		
		VFSContainer namedCourseFolder = new NamedContainerImpl(getTranslator().translate(NLS_FOLDER_DISPLAYNAME), rContainer);
		rootContainer = namedCourseFolder;
		FolderComponent folderComponent = new FolderComponent(ureq, "foldercomp", namedCourseFolder, null, null);
		folderComponent.addListener(this);
		cmdUpload = new CmdUpload(ureq, getWindowControl(), false, false);
		cmdUpload.execute(folderComponent, ureq, getTranslator(), true);		
		cmdUpload.hideFieldset();
		listenTo(cmdUpload);
		StackedPanel mainPanel = new SimpleStackedPanel("upl");
		Component uploadComp = cmdUpload.getInitialComponent();
		if (uploadComp != null)	{
			mainPanel.pushContent(uploadComp);
			fileChooser.put(mainPanel.getComponentName(), mainPanel);
		} else { // quota exceeded or no valid upload comp.
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			myContent.contextPut("quotaover", msg);
			fileChooser.contextPut("quotaover", msg);
		}
		fileChooserActive = false;
		updateVelocityVariables(chosenFile);
		putInitialPanel(myContent);
	}	
	
	private VFSContainer doUnzip(VFSLeaf vfsItem, VFSContainer currentContainer, WindowControl wControl, UserRequest ureq) {
		String name = vfsItem.getName();
		// we make a new folder with the same name as the zip file
		String sZipContainer = name.substring(0, name.length() - 4);
		VFSContainer zipContainer = currentContainer.createChildContainer(sZipContainer);
		if (zipContainer == null) {
			// folder already exists... issue warning
			wControl.setError(getTranslator().translate(NLS_UNZIP_ALREADYEXISTS, new String[] {sZipContainer}));
			// selectionTree must be set here since it fires events which will get caught in event methods below
			initFileSelectionController(ureq); 
			return null;
		}
		if (!ZipUtil.unzip(vfsItem, zipContainer)) {
			// operation failed - rollback
			zipContainer.delete();
			return null;
		} else {
			// check quota
			long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
			if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
				// quota exceeded - rollback
				zipContainer.delete();
				wControl.setError(getTranslator().translate(NLS_QUOTAEXEEDED));
				return null;
			}
		}
		return zipContainer;
	}
	
	/**
	 * This method generates a selection tree for choosing one file.
	 * @param ureq
	 * @param vfsContainer
	 * @return
	 */
	private void initFileSelectionController(UserRequest ureq) {
		VFSContainer vfsRoot = new NamedContainerImpl(getTranslator().translate(NLS_FOLDER_DISPLAYNAME), rootContainer);
		VFSItemFilter typeFilter = null;
		if (!allFileSuffixesAllowed && allowedFileSuffixes != null) {
			typeFilter = new VFSItemFileTypeFilter(allowedFileSuffixes);
		}
		// Clanup old file chooser and open up new one
		removeAsListenerAndDispose(fileChooserCtr);
		fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), vfsRoot, typeFilter, true);
		listenTo(fileChooserCtr);
		// open modal dialog for file chooser
		removeAsListenerAndDispose(cmcSelectionTree);
		cmcSelectionTree = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), fileChooserCtr.getInitialComponent());
		cmcSelectionTree.activate();
		listenTo(cmcSelectionTree);
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == wysiwygCtr) {
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				cmcWysiwygCtr.deactivate();
				if (event == Event.DONE_EVENT) {
					fireEvent(ureq, FILE_CHANGED_EVENT);				
					if (fileChooserActive) cmcFileChooser.deactivate();
					fileChooserActive = false;
				}
			}
		} else if (source == this.cmdUpload) {
			if (event == FolderCommand.FOLDERCOMMAND_FINISHED) {
				String fileName = cmdUpload.getFileName();
				if (fileName == null) {	// cancel button pressed
					cmcSelectionTree.deactivate();
					fileChooserActive = false;
					return;
				}
				fileName = fileName.toLowerCase();
				if (!isAllowedFileSuffixes(fileName)) {					
					this.showError(NLS_ERROR_FILETYPE);
					if (cmdUpload.fileWasOverwritten().booleanValue()) return;
					// delete file
					VFSItem item = rootContainer.resolve(cmdUpload.getFileName());
						if (item != null && (item.canDelete() == VFSConstants.YES)) {
							if (item instanceof MetaTagged) {
								// delete all meta info
								MetaInfo meta = ((MetaTagged)item).getMetaInfo();
								if (meta != null) meta.deleteAll();
							}
							// delete the item itself
							item.delete(); 
						}
					return;
				} else {
					if (fileName.endsWith("zip")) {
						// unzip zip file
						VFSContainer zipContainer = doUnzip((VFSLeaf)rootContainer.resolve(cmdUpload.getFileName()), this.rootContainer, getWindowControl(), ureq);
						// choose start file
						if (zipContainer != null) {
							// selectionTree must be set here since it fires events which will get caught in event methods below
							initFileSelectionController(ureq);
						}
					} else {
						//HTML file
						this.chosenFile = "/" + cmdUpload.getFileName();
						cmcFileChooser.deactivate();
						fileChooserActive = false;
					}
					updateVelocityVariables(chosenFile);
					fireEvent(ureq, FILE_CHANGED_EVENT);
				}
				return;
			}
		} else if (source == cmcFileChooser){
			updateVelocityVariables(chosenFile);
			fileChooserActive = false;
			if(event == CloseableModalController.CLOSE_MODAL_EVENT) {
				newFileForm.formResetted(ureq);
			}
			
		} else if (source == newFileForm) { // make new file
			if (event == Event.DONE_EVENT) {
				String fileName = newFileForm.getNewFileName();				
				rootContainer.createChildLeaf(fileName);
				this.chosenFile = fileName;
								
				removeAsListenerAndDispose(wysiwygCtr);
				wysiwygCtr = createWysiwygController(ureq, getWindowControl(), rootContainer, chosenFile);				
				listenTo(wysiwygCtr);
				removeAsListenerAndDispose(cmcWysiwygCtr);
				cmcWysiwygCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), wysiwygCtr.getInitialComponent());
				listenTo(cmcWysiwygCtr);
				cmcWysiwygCtr.activate();
				
				updateVelocityVariables(chosenFile);
				fireEvent(ureq, FILE_CHANGED_EVENT);
			}
			
		} else if (source == fileChooserCtr) { // the user chose a file or cancelled file selection
			cmcSelectionTree.deactivate();
			if (event instanceof FileChoosenEvent) {
				chosenFile = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, rootContainer, null);				
				updateVelocityVariables(chosenFile);
				fireEvent(ureq, FILE_CHANGED_EVENT);
				cmcFileChooser.deactivate();
				fileChooserActive = false;
			}
		} else if (source == allowRelativeLinksForm) {
			if (event == Event.DONE_EVENT) {
				allowRelativeLinks = allowRelativeLinksForm.getAllowRelativeLinksConfig();
				fireEvent(ureq, ALLOW_RELATIVE_LINKS_CHANGED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {

		if (source == previewLink){
			removeAsListenerAndDispose(previewLayoutCtr);
			SinglePageController previewController = new SinglePageController(ureq, getWindowControl(), rootContainer, chosenFile, allowRelativeLinks);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, previewController.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(previewController);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		}
		// edit chosen file
		else if (source == editButton){ // edit the chosen file in the rich text editor
			if (chosenFile == null) {				
				showError(NLS_ERROR_CHOOSEFILEFIRST);
				return;
			}
			VFSItem vfsItem = rootContainer.resolve(chosenFile);
			if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) {				
				showError(NLS_ERROR_FILEDOESNOTEXIST);
				return;
			}

			String editFile;
			VFSContainer editRoot;
			if (allowRelativeLinks) {
				editRoot = rootContainer;
				editFile = chosenFile;
			} else {
				ContainerAndFile caf = VFSUtil.calculateSubRoot(rootContainer, chosenFile);
				editRoot = caf.getContainer();
				editFile = caf.getFileName();
			}
			
			removeAsListenerAndDispose(wysiwygCtr);
			wysiwygCtr = createWysiwygController(ureq, getWindowControl(), editRoot, editFile);			
			listenTo(wysiwygCtr);
			removeAsListenerAndDispose(cmcWysiwygCtr);
			cmcWysiwygCtr = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), wysiwygCtr.getInitialComponent());
			listenTo(cmcWysiwygCtr);
			cmcWysiwygCtr.activate();
			updateVelocityVariables(chosenFile);
			fireEvent(ureq, FILE_CONTENT_CHANGED_EVENT);
		}
		// delete the chosen file
		else if (source == deleteButton){
			if (chosenFile == null) {				
				showError(NLS_ERROR_CHOOSEFILEFIRST);
				return;
			}
			VFSItem vfsItem = rootContainer.resolve(chosenFile);
			if (vfsItem == null || !(vfsItem instanceof LocalFileImpl)) {				
				showError(NLS_ERROR_FILEDOESNOTEXIST);
				return;
			}
			File file = ((LocalFileImpl)vfsItem).getBasefile();
			if (!file.exists()) {				
				showError(NLS_ERROR_FILEDOESNOTEXIST);
				return;
			}
			FileUtils.deleteDirsAndFiles(file, false, false);
			chosenFile = null;
			updateVelocityVariables(chosenFile);
			fireEvent(ureq, FILE_CHANGED_EVENT);
		}
		// change the chosen file or choose it the first time
		else if (source == changeFileButtonOne || source == changeFileButtonTwo){
			updateVelocityVariables(chosenFile);
			removeAsListenerAndDispose(cmcFileChooser);
			cmcFileChooser = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), fileChooser);			
			listenTo(cmcFileChooser);
			cmcFileChooser.activate();
			
			fileChooserActive = true;
		}
		else if (source == fileChooser) { 
			if (event.getCommand().equals(ACTION_CHANGE)) {
				if (chosenFile == null) {					
					showError(NLS_ERROR_CHOOSEFILEFIRST);
					return;
				}
				cmcFileChooser.deactivate();
				updateVelocityVariables(chosenFile);
			}
		}
		// file chosen or "rechoose" pressed
		else if (source == chooseFileButton){
			initFileSelectionController(ureq);
		}
	}
	
	/**
	 * @return The choosen file name
	 */
	public String getChosenFile(){
	    return chosenFile;
	}
	
	public boolean isEditorEnabled() {
		Boolean editable = (Boolean)myContent.getContext().get(VC_ENABLEEDIT);
		return editable != null && editable.booleanValue();
	}
	
	/**
	 * @return The configuration for the allow relative links flag
	 */
	public Boolean getAllowRelativeLinks() {
		return allowRelativeLinks;
	}
	
	/**
	 * Update all velocity variables: push file, push / remove form etc
	 * @param chosenFile
	 */
	private void updateVelocityVariables(String file) {
		cmdUpload.refreshActualFolderUsage();
		if (file != null) {
			previewLink.setCustomDisplayText(getTranslator().translate(NLS_FOLDER_DISPLAYNAME) + file);
			myContent.contextPut(VC_CHANGE, Boolean.TRUE);
			myContent.contextPut(VC_CHOSENFILE, file);
			fileChooser.contextPut(VC_CHOSENFILE, file);
			myContent.contextPut(VC_FILE_IS_CHOSEN, Boolean.TRUE);
			fileChooser.contextPut(VC_FILE_IS_CHOSEN, Boolean.TRUE);
			myContent.contextPut(VC_ENABLEDELETE, Boolean.TRUE);
			// add form to velocity
			myContent.put("allowRelativeLinksForm", allowRelativeLinksForm.getInitialComponent());
			if (file.toLowerCase().endsWith(".html") || file.toLowerCase().endsWith(".htm")) {
				myContent.contextPut(VC_ENABLEEDIT, Boolean.TRUE);
			} else {
				myContent.contextPut(VC_ENABLEEDIT, Boolean.FALSE);
			}
		} else {
			myContent.contextPut(VC_CHANGE, Boolean.FALSE);
			fileChooser.contextPut(VC_CHANGE, Boolean.FALSE);			
			myContent.contextPut(VC_CHOSENFILE, getTranslator().translate(NLS_NO_FILE_CHOSEN));
			fileChooser.contextPut(VC_CHOSENFILE, getTranslator().translate(NLS_NO_FILE_CHOSEN));
			myContent.contextPut(VC_FILE_IS_CHOSEN, Boolean.FALSE);
			fileChooser.contextPut(VC_FILE_IS_CHOSEN, Boolean.FALSE);
			myContent.contextPut(VC_ENABLEEDIT, Boolean.FALSE);
			myContent.contextPut(VC_ENABLEDELETE, Boolean.FALSE);
			// remove form from velocity
			myContent.remove(allowRelativeLinksForm.getInitialComponent());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// child controllers autodisposed by basic controller
	}
	
	/**
	 * All types of files are allowed.
	 * @param allowed
	 */
	public void setAllFileSuffixesAllowed(boolean allowed) {
		this.allFileSuffixesAllowed = allowed;
	}

	/**
	 * Setting supported file-suffix
	 * @param allowedFileSuffixes  New list of allowed file-suffix e.g. html, htm
	 */
	public void setAllowedFileSuffixes(String[] allowedFileSuffixes) {
		this.allowedFileSuffixes = allowedFileSuffixes;
		//TODO if not standard, remove reference to HTML pages
	}
	
	protected Controller createWysiwygController(UserRequest ureq, WindowControl windowControl, VFSContainer fileContainer, String fileToEdit) {
	  return WysiwygFactory.createWysiwygController(ureq, windowControl, fileContainer, fileToEdit, true, true);
	}
	
	/**
	 * Check if a filename has a valid suffix. Allowed suffix are e.g. '.zip','.html','.xml'
	 * ZIP files are allways allowed, all other suffix depends on allowedFileSuffixes array
	 * or from the flag allFileSuffixesAllowed. 
	 * @param fileName
	 * @return true : Suffix allowed
	 *         false: Suffix NOT allowed
	 */
	private boolean isAllowedFileSuffixes(String fileName) {
		fileName = fileName.toLowerCase();
		if(allFileSuffixesAllowed) {
			return true;
		}
		
		if (fileName.endsWith(".zip")) {
			return true;
		}
		for (int i = 0; i < allowedFileSuffixes.length; i++) {
			if (fileName.endsWith("." + allowedFileSuffixes[i]) ) {
				return true;
			}
		}
		return false;
	}

}


class NewFileForm extends FormBasicController {
	
	private TextElement textElement;
	private Submit createFile;
	private VFSContainer rootContainer;
	private String newFileName;
		
		
	public NewFileForm(UserRequest ureq, WindowControl wControl, Translator translator, VFSContainer rootContainer) {
		super(ureq, wControl);
		this.rootContainer = rootContainer;
		setTranslator(translator);
		
		initForm(ureq);		
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {						
		textElement = FormUIFactory.getInstance().addTextElement("fileName", "newfile", 20, "", formLayout);
		textElement.setMandatory(true);
		
		createFile = new FormSubmit("submit","button.create");
		formLayout.add(createFile);				
	}	
	
	@Override
	protected void doDispose() {
		//nothing to dispose
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String fileName = textElement.getValue();		
		if(fileName==null || fileName.trim().equals("")) {
			textElement.setErrorKey("error.name.empty", new String[0]);
			isInputValid = false;
		} else {
			fileName = fileName.toLowerCase();
			// check if there are any unwanted path denominators in the name
			if (!validateFileName(fileName)) {
				textElement.setErrorKey("error.filename", new String[0]);
				isInputValid = false;
				return isInputValid;
			} else if (!fileName.endsWith(".html") && !fileName.endsWith(".htm")) {
        //add html extension if missing
				fileName = fileName + ".html";
			}
			if (fileName.charAt(0) != '/') fileName = '/' + fileName;
			VFSItem vfsItem = rootContainer.resolve(fileName);
			if (vfsItem != null) {
				textElement.setErrorKey("error.fileExists", new String[] {fileName});
				isInputValid = false;
			} else {
				newFileName = fileName;
				isInputValid = true;				
			}
		}			
		return isInputValid;			
	}
	
	private boolean validateFileName(String name) {
		boolean isValid = true;			
    //check if there are any unwanted path denominators in the name
		if (name.indexOf("..") > -1 || name.indexOf('/') > -1 || name.indexOf('\\')>-1) {
			isValid = false;
		}
		return isValid;
	}			
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);  
	}
	
	@Override
	protected void formResetted(UserRequest ureq) {
		textElement.reset();
		fireEvent(ureq, Event.CANCELLED_EVENT);      
	}	

	/**
	 * @return the new file name
	 */
	public String getNewFileName() {
		return newFileName;
	}

}


class AllowRelativeLinksForm extends FormBasicController {
	
	private SelectionElement allowRelativeLinks;
	private boolean isOn;

	/**
	 * @param allowRelativeLinksConfig
	 * @param trans
	 */
	AllowRelativeLinksForm(UserRequest ureq, WindowControl wControl, Boolean allowRelativeLinksConfig) {
			super(ureq, wControl);
			isOn = allowRelativeLinksConfig != null && allowRelativeLinksConfig.booleanValue();
			initForm (ureq);
	}


	/**
	 * @return Boolean new configuration
	 */
	boolean getAllowRelativeLinksConfig(){
		return allowRelativeLinks.isSelected(0);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no explicit submit button, DONE event fired every time the checkbox is clicked
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		allowRelativeLinks = uifactory.addCheckboxesHorizontal("allowRelativeLinks", "allowRelativeLinks", formLayout, new String[] {"xx"}, new String[] {null});
		allowRelativeLinks.select("xx", isOn);
		allowRelativeLinks.addActionListener(FormEvent.ONCLICK);
	}

	@Override
	protected void doDispose() {
		//	
	}
	
}