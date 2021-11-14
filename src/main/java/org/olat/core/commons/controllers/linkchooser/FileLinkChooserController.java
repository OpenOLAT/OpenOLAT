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

package org.olat.core.commons.controllers.linkchooser;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.core.util.vfs.filters.VFSItemFileTypeFilter;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * enclosing_type Description: <br>
 * this controller is used to generate a component containing the provided
 * menutree, the tool, and the content. its main use is to standardize the look
 * and feel of workflows that contain both a menu and a tool
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELLED_EVENT
 * <li>URLChoosenEvent(URL) containing the selected file URL
 * </ul>
 * @author Felix Jost
 */
public class FileLinkChooserController extends BasicController {

	private VelocityContainer mainVC;

	private FileUploadController uploadCtr;
	private org.olat.core.commons.controllers.filechooser.FileChooserController fileChooserController;

	private final String fileName;
	private String[] suffixes;
	private final String absolutePath;
	private VFSContainer rootDir;
	@Autowired
	private MovieService movieService;

	/**
	 * @param ureq
	 * @param wControl
	 * @param rootDir The VFS root directory from which the linkable files should be read
	 * @param uploadRelPath The relative path within the rootDir where uploaded
	 *          files should be put into. If NULL, the root Dir is used
	 * @param absolutePath
	 * @param suffixes Array of allowed file types
	 * @param uriValidation Set to true if the filename need to be a valid URI
	 * @param fileName the path of the file currently edited (in order to compute
	 *          the correct relative paths for links), e.g. bla/blu.html or
	 *          index.html
	 */
	public FileLinkChooserController(UserRequest ureq, WindowControl wControl,
			VFSContainer rootDir, String uploadRelPath, String absolutePath, String[] suffixes,
			boolean uriValidation, String fileName) {
		super(ureq, wControl);
		this.fileName = fileName;
		this.suffixes = suffixes;
		this.rootDir = rootDir;
		this.absolutePath = absolutePath;
		this.mainVC = createVelocityContainer("filechooser");

		// file uploads are relative to the currently edited file 
		String[] dirs = (this.fileName == null) ? new String[0] : this.fileName.split("/");
		VFSContainer fileUploadBase = rootDir;
		for (String subPath : dirs) {
			// try to resolve the given file path in the root container
			VFSItem subFolder = fileUploadBase.resolve(subPath);
			if (subFolder != null) {
				if (subFolder instanceof VFSContainer) {
					// a higher level found, use this one unless a better one is found
					fileUploadBase = (VFSContainer) subFolder;
				} else {
					// it is not a container - leaf reached
					break;
				}
			} else {
				// resolving was not possible??? stop here
				break;
			}
		}
		// create directory filter combined with suffix filter
		String[] dirFilters = { "_courseelementdata" };
		VFSItemFilter customFilter = null;
		VFSItemFilter dirFilter = new VFSItemExcludePrefixFilter(dirFilters, true);
		if (suffixes != null) {
			VFSItemFileTypeFilter typeFilter = new VFSItemFileTypeFilter(suffixes, true, uriValidation);
			typeFilter.setCompositeFilter(dirFilter);
			customFilter = typeFilter;
		} else {
			customFilter = dirFilter;
		}
		
		
		// hide file chooser title, we have our own title
		fileChooserController = FileChooserUIFactory
				.createFileChooserControllerWithoutTitle(ureq, getWindowControl(), rootDir,
						customFilter, true);
		listenTo(fileChooserController);
		mainVC.put("stTree", fileChooserController.getInitialComponent());

		// convert file endings to mime types as needed by file upload controller
		Set<String> mimeTypes = null;
		if (suffixes != null) {
			mimeTypes = new HashSet<>();
			for (String suffix : suffixes) {
				String mimeType = WebappHelper.getMimeType("dummy." + suffix);
				if (mimeType != null) {
					if (!mimeTypes.contains(mimeType)) mimeTypes.add(mimeType);
				}
			}
		}

		if(fileUploadBase.canWrite() == VFSConstants.YES) {
			long remainingSpace = Quota.UNLIMITED;
			long uploadLimit = FolderConfig.getLimitULKB();
			if( fileUploadBase.getLocalSecurityCallback() != null && fileUploadBase.getLocalSecurityCallback().getQuota() != null) {
				Long space = fileUploadBase.getLocalSecurityCallback().getQuota().getRemainingSpace();
				if(space != null) {
					remainingSpace = space.longValue();
				}
				Long limit = fileUploadBase.getLocalSecurityCallback().getQuota().getUlLimitKB();
				if(limit != null) {
					uploadLimit = limit.longValue();
				}
			}
			
			uploadCtr = new FileUploadController(wControl, fileUploadBase, ureq, uploadLimit, remainingSpace,
					mimeTypes, uriValidation, true, false, true, true, false);
			listenTo(uploadCtr);
			// set specific upload path
			uploadCtr.setUploadRelPath(uploadRelPath);
			mainVC.put("uploader", uploadCtr.getInitialComponent());
		}

		putInitialPanel(mainVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == uploadCtr) {
			if (event instanceof FolderEvent) {
				FolderEvent folderEvent = (FolderEvent) event;
				if (isFileSuffixOk(folderEvent.getFilename())) {
					Size size = null;
					VFSItem item = folderEvent.getItem();
					
					String relPath;
					if(item != null) {
						size = getSize(item, item.getName());
						relPath = FileChooserUIFactory
								.getSelectedRelativeItemPath(folderEvent, rootDir, fileName);
					} else {
						relPath = folderEvent.getFilename();
					}
					
					if(StringHelper.containsNonWhitespace(absolutePath)) {
						relPath = absolutePath + relPath;
					}
					
					if(size != null) {
						fireEvent(ureq, new URLChoosenEvent(relPath, null, null, null, size.getWidth(), size.getHeight()));
					} else {
						fireEvent(ureq, new URLChoosenEvent(relPath));
					}
				} else {
					setErrorMessage(folderEvent.getFilename());
				}
			}
			if (event == Event.DONE_EVENT) {
				if (uploadCtr.getStatus() == FolderCommandStatus.STATUS_CANCELED) {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if (event == Event.FAILED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (source == fileChooserController) {
			if (event instanceof FileChoosenEvent) {
				FileChoosenEvent fileEvent = (FileChoosenEvent)event;
				VFSItem item = fileEvent.getSelectedItem();
				Size size = getSize(item, item.getName());
				
				String relPath = FileChooserUIFactory
						.getSelectedRelativeItemPath(fileEvent, rootDir, fileName);
				// notify parent controller
				if(StringHelper.containsNonWhitespace(absolutePath)) {
					relPath = absolutePath + relPath;
				}
				
				if(size != null) {
					fireEvent(ureq, new URLChoosenEvent(relPath, null, null, null, size.getWidth(), size.getHeight()));
				} else {
					fireEvent(ureq, new URLChoosenEvent(relPath));
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);

			} else if (event == Event.FAILED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}
	
	private Size getSize(VFSItem item, String filename) {
		Size size = null;
		if(item instanceof VFSLeaf) {
			VFSLeaf leaf = (VFSLeaf)item;
			String suffix = FileUtils.getFileSuffix(filename);
			size = movieService.getSize(leaf, suffix);
		}
		return size;
	}

	private boolean isFileSuffixOk(String filename) {
		if (suffixes == null) {
			// no defined suffixes => all allowed
			return true;
		} else {
			// check if suffix one of allowed suffixes
			String suffix = getSuffix(filename);
			for (String allowedSuffix : suffixes) {
				if (allowedSuffix.equals(suffix)) {
					return true;
				}
			}
		}
		return false;
	}

	private void setErrorMessage(String fileName) {
		StringBuilder allowedSuffixes = new StringBuilder();
		for (String allowedSuffix : suffixes) {
			allowedSuffixes.append(" .");
			allowedSuffixes.append(allowedSuffix);
		}
		String suffix = getSuffix(fileName);
		getWindowControl().setError(getTranslator()
				.translate("upload.error.incorrect.filetype",
						new String[] { "." + suffix,allowedSuffixes.toString() }));
	}

	private String getSuffix(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}
}