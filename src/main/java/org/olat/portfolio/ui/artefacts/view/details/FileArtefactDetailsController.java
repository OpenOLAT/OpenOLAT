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
package org.olat.portfolio.ui.artefacts.view.details;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.artefacts.FileArtefact;
import org.olat.portfolio.ui.artefacts.collect.EPCreateFileArtefactStepForm00;

/**
 * Description:<br>
 * show specific infos for FileArtefact 
 * allow to delete / upload a file
 * 
 * <P>
 * Initial Date:  08.10.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class FileArtefactDetailsController extends BasicController {

	private VelocityContainer vC;
	private boolean readOnlyMode;
	MediaResource mr;
	private Link delLink;
	private DialogBoxController delDialog;
	private FileArtefact fArtefact;
	private Controller fileUploadCtrl;
	private EPFrontendManager ePFMgr;
	private Link uploadLink;
	private CloseableCalloutWindowController calloutCtrl;
	private Panel viewPanel;

	public FileArtefactDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		super(ureq, wControl);
		this.readOnlyMode = readOnlyMode;
		fArtefact = (FileArtefact)artefact;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		
		viewPanel = new Panel("empty");
		initViewDependingOnFileExistance(ureq);
		
		putInitialPanel(viewPanel);		
	}
	
	private void initViewDependingOnFileExistance(UserRequest ureq){
		VFSItem file = ePFMgr.getArtefactContainer(fArtefact).resolve(fArtefact.getFilename());
		if (file != null && file instanceof VFSLeaf) {
			initFileView(file, ureq);
		} else if (!readOnlyMode){
			initUploadView();			
		}
	}
	
	private void initFileView(VFSItem file, UserRequest ureq){
		vC = createVelocityContainer("fileDetails");
		DownloadComponent downlC = new DownloadComponent("download", (VFSLeaf) file);
		vC.put("download", downlC);	
		vC.contextPut("filename", fArtefact.getFilename());
		
		if(file.canMeta() == VFSConstants.YES) {
			MetaInfo meta = file.getMetaInfo();
			vC.contextPut("meta", meta);
			// show a preview thumbnail if possible
			if (meta.isThumbnailAvailable()) {
				VFSLeaf thumb = meta.getThumbnail(200, 200, false);
				if(thumb != null) {
					mr = new VFSMediaResource(thumb);
				}
				if(mr != null) {
					String thumbMapper = registerMapper(ureq, (relPath, request) -> {
						return mr;
					});					
					vC.contextPut("thumbMapper", thumbMapper);
				}
			}
		}
		if (!readOnlyMode){
			// allow to delete
			delLink = LinkFactory.createLink("delete.file", vC, this);
			delLink.setUserObject(file);
		}
		
		viewPanel.setContent(vC);
	}
	
	private void initUploadView(){
		vC = createVelocityContainer("fileDetailsUpload");
		uploadLink = LinkFactory.createLink("upload.link", vC, this);
		viewPanel.setContent(vC);
	}
	
	private void popupUploadCallout(UserRequest ureq){
		removeAsListenerAndDispose(fileUploadCtrl);
		fileUploadCtrl = new EPCreateFileArtefactStepForm00(ureq, getWindowControl(), fArtefact);
		listenTo(fileUploadCtrl);		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), fileUploadCtrl.getInitialComponent(), uploadLink, fArtefact.getTitle(), true, null);
		calloutCtrl.addDisposableChildController(fileUploadCtrl);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == delLink) {			
			delDialog = activateYesNoDialog(ureq, translate("delete.file"), translate("delete.dialog"), delDialog);
			delDialog.setUserObject(delLink.getUserObject());
		}	else if (source == uploadLink){
			popupUploadCallout(ureq);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == delDialog && DialogBoxUIFactory.isYesEvent(event)) { 
			VFSItem artefactFile = (VFSItem) delDialog.getUserObject();
			artefactFile.delete();
			fArtefact.setFilename("");
			ePFMgr.updateArtefact(fArtefact);
			initViewDependingOnFileExistance(ureq);
		} else if (source == fileUploadCtrl){
			calloutCtrl.deactivate();
			removeAsListenerAndDispose(calloutCtrl);			
			ePFMgr.updateArtefact(fArtefact);
			initViewDependingOnFileExistance(ureq);
		} else if (source == calloutCtrl && event.equals(CloseableCalloutWindowController.CLOSE_WINDOW_EVENT)) {
			removeAsListenerAndDispose(calloutCtrl);
			calloutCtrl = null;
		}
	}

	@Override
	protected void doDispose() {
		// nothing		
	}
	
}

