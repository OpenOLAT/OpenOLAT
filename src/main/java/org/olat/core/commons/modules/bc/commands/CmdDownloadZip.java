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
package org.olat.core.commons.modules.bc.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * 
 * Initial date: 31.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CmdDownloadZip implements FolderCommand {
	
	private static final Logger log = Tracing.createLoggerFor(CmdDownloadZip.class);

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
		VFSContainer currentContainer = folderComponent.getCurrentContainer();

		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
	
		FileSelection selection = new FileSelection(ureq, folderComponent.getCurrentContainer(), folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		if(selection.getFiles().isEmpty()) {
			status = FolderCommandStatus.STATUS_FAILED;
			wControl.setWarning(trans.translate("warning.file.selection.empty22"));
			return null;
		}
		
		MediaResource mr = new ZipMediaResource(currentContainer, selection);
		ureq.getDispatchResult().setResultingMediaResource(mr);
		return null;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return false;
	}

	@Override
	public String getModalTitle() {
		return null;
	}

	private static class ZipMediaResource implements MediaResource {
		
		private final FileSelection selection;
		private final VFSContainer currentContainer;
		
		public ZipMediaResource(VFSContainer currentContainer, FileSelection selection) {
			this.selection = selection;
			this.currentContainer = currentContainer;
		}
		
		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_NO_CACHE;
		}

		@Override
		public boolean acceptRanges() {
			return false;
		}

		@Override
		public String getContentType() {
			return "application/zip";
		}

		@Override
		public Long getSize() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return null;
		}

		@Override
		public Long getLastModified() {
			return null;
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			List<String> selectedFiles = selection.getFiles();
			
			String urlEncodedLabel;
			if(selectedFiles.size() == 1) {
				String filename = selectedFiles.get(0);
				int lastIndexOf = filename.lastIndexOf('.');
				if(lastIndexOf > 0) {
					filename = filename.substring(0, lastIndexOf);
				}
				urlEncodedLabel = StringHelper.urlEncodeUTF8(filename + ".zip");
			} else  {
				urlEncodedLabel = "Archive.zip";
			}
			
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
			hres.setHeader("Content-Description", urlEncodedLabel);
			
			if(selectedFiles.size() == 1 && selectedFiles.get(0).toLowerCase().endsWith(".zip")) {
				VFSItem singleItem = currentContainer.resolve(selectedFiles.get(0));
				if(singleItem instanceof VFSLeaf) {
					try(OutputStream out = hres.getOutputStream()) {
						VFSManager.copyContent((VFSLeaf)singleItem, out);
					} catch(IOException e) {
						log.error("", e);
					}
				} else {
					prepareZip(hres, selectedFiles);
				}
			} else {
				prepareZip(hres, selectedFiles);
			}
		}
		
		private void prepareZip(HttpServletResponse hres, List<String> selectedFiles) {
			VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
				zout.setLevel(9);
				
				List<VFSItem> vfsFiles = new ArrayList<>();
				for (String fileName : selectedFiles) {
					VFSItem item = currentContainer.resolve(fileName);
					if (item != null) {
						vfsFiles.add(item);
						// update download counter
						if (item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
							vfsRepositoryService.increaseDownloadCount((VFSLeaf)item);
						}
					}
				}
				
				for (VFSItem item:vfsFiles) {
					ZipUtil.addToZip(item, "", zout, new VFSSystemItemFilter(), false);
				}
				zout.flush();
			} catch (IOException e) {
				String className = e.getClass().getSimpleName();
				if("ClientAbortException".equals(className)) {
					log.debug("client browser probably abort when downloading zipped files", e);
				} else {
					log.error("client browser probably abort when downloading zipped files", e);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}

		@Override
		public void release() {
			//
		}
	}
}