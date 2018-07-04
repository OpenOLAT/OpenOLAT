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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 31.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CmdDownloadZip implements FolderCommand {
	
	private static final OLog log = Tracing.createLoggerFor(CmdDownloadZip.class);

	private FileSelection selection;
	private VFSContainer currentContainer;
	private int status = FolderCommandStatus.STATUS_SUCCESS;

	
	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
		currentContainer = folderComponent.getCurrentContainer();

		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
	
		selection = new FileSelection(ureq, folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		if(selection.getFiles().isEmpty()) {
			status = FolderCommandStatus.STATUS_FAILED;
			wControl.setWarning(trans.translate("warning.file.selection.empty"));
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
			
			try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
				zout.setLevel(9);
				
				List<VFSItem> vfsFiles = new ArrayList<>();
				for (String fileName : selectedFiles) {
					VFSItem item = currentContainer.resolve(fileName);
					if (item != null) {
						vfsFiles.add(item);
						// update download counter
						if (item instanceof MetaTagged) {
							MetaTagged itemWithMeta = (MetaTagged) item;
							MetaInfo meta = itemWithMeta.getMetaInfo();
							meta.increaseDownloadCount();
							meta.write();
						}
					}
				}
				
				boolean success = true;
				for (Iterator<VFSItem> iter = vfsFiles.iterator(); success && iter.hasNext();) {
					success = ZipUtil.addToZip(iter.next(), "", zout);
				}
				zout.flush();
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