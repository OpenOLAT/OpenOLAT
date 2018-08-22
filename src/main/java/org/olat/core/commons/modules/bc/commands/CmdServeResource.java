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

package org.olat.core.commons.modules.bc.commands;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.modules.bc.FolderLoggingAction;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

public class CmdServeResource implements FolderCommand {

	private static final String DEFAULT_ENCODING = "iso-8859-1";
	private static final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta.*charset=([^\"]*)\"", Pattern.CASE_INSENSITIVE);

	// the latest encoding is saved since .js files loaded by the browser are
	// assumed to have the same encoding as the html page
	private String g_encoding;
	
	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		VFSSecurityCallback inheritedSecCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
		if (inheritedSecCallback != null && !inheritedSecCallback.canRead())
			throw new RuntimeException("Illegal read attempt: " + folderComponent.getCurrentContainerPath());
		
		// extract file
		String path = ureq.getModuleURI();
		VFSItem vfsitem = folderComponent.getRootContainer().resolve(path);
		if(vfsitem == null) {
			//double decoding of ++
			vfsitem = FolderCommandHelper.tryDoubleDecoding(ureq, folderComponent);
		}
		MediaResource mr = getMediaResource(path, vfsitem);
		ThreadLocalUserActivityLogger.log(FolderLoggingAction.BC_FILE_READ, getClass(), CoreLoggingResourceable.wrapBCFile(path));
		ureq.getDispatchResult().setResultingMediaResource(mr);
		
		// update download counter
		if (vfsitem instanceof MetaTagged) {
			MetaTagged itemWithMeta = (MetaTagged) vfsitem;
			MetaInfo meta = itemWithMeta.getMetaInfo();
			meta.increaseDownloadCount();
			meta.write();
		}

		return null;
	}
	
	public MediaResource getMediaResource(String path, VFSItem vfsitem) {
		MediaResource mr = null;
		if (vfsitem == null) {
			mr = new NotFoundMediaResource();
		} else if(!(vfsitem instanceof VFSLeaf)) {
			mr = new NotFoundMediaResource();
		} else {
			
			VFSLeaf vfsfile = (VFSLeaf)vfsitem;
			boolean forceDownload = FolderManager.isDownloadForcedFileType(vfsfile.getName());
			if (path.toLowerCase().endsWith(".html") || path.toLowerCase().endsWith(".htm")) {
				// set the http content-type and the encoding
				// try to load in iso-8859-1
				InputStream is = vfsfile.getInputStream();
				if(is == null) {
					mr = new NotFoundMediaResource();
				} else {
					String page = FileUtils.load(is, DEFAULT_ENCODING);
					// search for the <meta content="text/html; charset=utf-8"
					// http-equiv="Content-Type" /> tag
					// if none found, assume iso-8859-1
					String enc = DEFAULT_ENCODING;
					boolean useLoaded = false;
					// <meta.*charset=([^"]*)"
					Matcher m = PATTERN_ENCTYPE.matcher(page);
					boolean found = m.find();
					if (found) {
						String htmlcharset = m.group(1);
						enc = htmlcharset;
						if (htmlcharset.equals(DEFAULT_ENCODING)) {
							useLoaded = true;
						}
					} else {
						useLoaded = true;
					}
					// set the new encoding to remember for any following .js file loads
					g_encoding = enc;
					if (useLoaded) {
						StringMediaResource smr = new StringMediaResource();
						String mimetype = forceDownload ? VFSMediaResource.MIME_TYPE_FORCE_DOWNLOAD : "text/html;charset=" + enc;
						smr.setContentType(mimetype);
						smr.setEncoding(enc);
						smr.setData(page);
						if(forceDownload) {
							smr.setDownloadable(true, vfsfile.getName());
						}
						mr = smr;
					} else {
						// found a new charset other than iso-8859-1 -> let it load again
						// as bytes (so we do not need to convert to string and back
						// again)
						VFSMediaResource vmr = new VFSMediaResource(vfsfile);
						vmr.setEncoding(enc);
						if(forceDownload) {
							vmr.setDownloadable(true);
						}
						mr = vmr;
					}
				}
			} else if (path.endsWith(".js")) { // a javascript library
				VFSMediaResource vmr = new VFSMediaResource(vfsfile);
				// set the encoding; could be null if this page starts with .js file
				// (not very common...).
				// if we set no header here, apache sends the default encoding
				// together with the mime-type, which is wrong.
				// so we assume the .js file has the same encoding as the html file
				// that loads the .js file
				if (g_encoding != null) {
					vmr.setEncoding(g_encoding);
				}
				if(forceDownload) {
					vmr.setDownloadable(true);
				}
				mr = vmr;
			} else if (path.endsWith(".txt")) {
				//text files created in OpenOLAT are utf-8, prefer this encoding
				VFSMediaResource vmr = new VFSMediaResource(vfsfile);
				vmr.setEncoding("utf-8");
				if(forceDownload) {
					vmr.setDownloadable(true);
				}
				mr = vmr;
			} else {
				// binary data: not .html, not .htm, not .js -> treated as is
				VFSMediaResource vmr = new VFSMediaResource(vfsfile);
				if(forceDownload) {
					vmr.setDownloadable(true);
				}
				mr = vmr;
			}
		}
		return mr;
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
}
