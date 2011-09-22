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
* <p>
*/ 

package org.olat.repository.controllers;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.olat.admin.quota.QuotaConstants;
import org.olat.commons.file.filechooser.FileChooserController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemFileTypeFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.AddingResourceException;
import org.olat.fileresource.types.FileResource;
import org.olat.repository.RepositoryEntry;

/**
 * <!--**************-->
 * <h3>Responsability:</h3>
 * Offers to choose a file which is then added to the repository. Chooseable
 * files can be restricted to specific file types.
 * <p>
 * <!--**************-->
 * <h3>Events fired:</h3>
 * <i>No events are fired!</i><br>
 * Signals are sent via
 * {@link org.olat.repository.controllers.RepositoryAddCallback RepositoryAddCallback}
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * Choose the file from a personal folder or upload one.<br>
 * Add file resource to repository.<br>
 * Signal success via
 * {@link org.olat.repository.controllers.RepositoryAddCallback#finished(UserRequest) finished(..)}</li>
 * <li><i>Failure:</i><br>
 * Wrong type of file or file creation failed.<br>
 * Signal failure via
 * {@link org.olat.repository.controllers.RepositoryAddCallback#failed(UserRequest) failure(..)}.</li>
 * <li><i>Cancel:</i><br>
 * Choosing file is canceled<br>
 * Signal cancel via
 * {@link org.olat.repository.controllers.RepositoryAddCallback#canceled(UserRequest) canceled(..)}</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3>
 * It is a special kind of controller as it uses the
 * {@link org.olat.repository.controllers.RepositoryAddCallback RepositoryAddCallback}
 * to signal state changes TODO:pb:a link to the doc book explaining how the
 * repository is used.
 * <p>
 * 
 * @author Felix Jost
 */
public class AddFileResourceController extends BasicController implements IAddController {

	private FileChooserController cfc;
	private RepositoryAddCallback addCallback;
	private List limitTypes;
	private FileResource newFileResource;

	/**
	 * Controller implementing "Repository Add"-workflow for all file resources.
	 * 
	 * @param addCallback the channel to signal success, failure, cancel
	 * @param limitTypes may be <code>null</code> indicating no limits.
	 * @param ureq
	 * @param wControl
	 */
	public AddFileResourceController(RepositoryAddCallback addCallback, List limitTypes, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init(addCallback, limitTypes, null, ureq);
	}

	/**
	 * Same as above, but able to restrict filetypes, this controller accepts.
	 * 
	 * @param addCallback the channel to signal success, failure, cancel
	 * @param limitTypes may be <code>null</code> inidicating no limits.
	 * @param suffixFilter may be <code>null</code> for having no filter.
	 * @param ureq
	 * @param wControl
	 */
	public AddFileResourceController(RepositoryAddCallback addCallback, List limitTypes, String[] suffixFilter, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init(addCallback, limitTypes, suffixFilter, ureq);
	}

	private void init(RepositoryAddCallback rac, List lt, String[] suffixFilter, UserRequest ureq) {

		this.addCallback = rac;
		this.limitTypes = lt;
		
		// prepare generic filechoser for add file
		removeAsListenerAndDispose(cfc);
		cfc = new FileChooserController(ureq, getWindowControl(), (int)QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO).getUlLimitKB().longValue(), false);
		listenTo(cfc);
		
		if (suffixFilter != null) {
			VFSItemFileTypeFilter fypeFilter = new VFSItemFileTypeFilter(suffixFilter);
			cfc.setSuffixFilter(fypeFilter);
		}
		putInitialPanel(cfc.getInitialComponent());
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		return getInitialComponent();
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		// Nothing to do here. file resource already created.
		return true;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		// File resource already created. Cleanup file resource on abort.
		if (newFileResource != null) FileResourceManager.getInstance().deleteFileResource(newFileResource);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to process here.
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == cfc) {
			if (event.equals(Event.DONE_EVENT)) {
				// create new repository entry

				File f;
				String fName;
				if (cfc.isFileFromFolder()) {
					VFSLeaf vfsLeaf = cfc.getFileSelection();
					if (!(vfsLeaf instanceof LocalFileImpl)) {
						showError("add.failed");
						addCallback.failed(urequest);
						return;
					}
					f = ((LocalFileImpl) vfsLeaf).getBasefile();
					fName = f.getName();
				} else {
					f = cfc.getUploadedFile();
					fName = cfc.getUploadedFileName();
				}
				FileResourceManager frm = FileResourceManager.getInstance();
				if (f!=null && !f.exists()) {
					// if f does not exist at this point then something went wrong with the upload
					// inform the user rather than issuing a log.error in FileUtils (OLAT-5383/OLAT-5384)
					showError("Failed");
					addCallback.failed(urequest);
					return;
				}
				try {				
					newFileResource = frm.addFileResource(f, fName);
				} catch (AddingResourceException e) {
					showError(e.getErrorKey());
					addCallback.failed(urequest);
					return;
				}
				if (newFileResource == null) {
					showError("add.failed");
					addCallback.failed(urequest);
					return;
				}

				if (limitTypes != null && limitTypes.size() > 0) { // check for type
					boolean validType = false;
					for (Iterator iter = limitTypes.iterator(); iter.hasNext();) {
						String limitType = (String) iter.next();
						if (newFileResource.getResourceableTypeName().equals(limitType)) validType = true;
					}
					if (!validType) { // rollback
						frm.deleteFileResource(newFileResource);
						showError("add.wrongtype");
						addCallback.failed(urequest);
						return;
					}
				}
				
				// cleanup tmp files and directories
				// cfc.release(); happens in dispose of cfc

				addCallback.setResourceable(newFileResource);
				addCallback.setResourceName(fName);
				String displayName = fName;
				int lastDot = displayName.lastIndexOf('.');
				if (lastDot != -1) displayName = displayName.substring(0, lastDot);
				addCallback.setDisplayName(displayName);
				addCallback.finished(urequest);
				return;
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				addCallback.canceled(urequest);
				return;
			}
		}
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		// nothing to do here.
		return;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
