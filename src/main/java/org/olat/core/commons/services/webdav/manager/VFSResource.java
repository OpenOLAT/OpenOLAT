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
package org.olat.core.commons.services.webdav.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.webdav.servlets.ConcurrentDateFormat;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSResource implements WebResource {
	
	private static final OLog log = Tracing.createLoggerFor(VFSResource.class);
	
	private final VFSItem item;
	private final String path;
	private String mimeType;
    private volatile String weakETag;

	public VFSResource(VFSItem item, String path) {
		this.item = item;
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	public VFSItem getItem() {
		return item;
	}

	@Override
	public long getLastModified() {
		return item.getLastModified();
	}

	@Override
	public String getLastModifiedHttp() {
		return ConcurrentDateFormat.formatRfc1123(new Date(getLastModified()));
	}

	@Override
	public boolean exists() {
		return item != null && item.exists();
	}

	@Override
	public boolean isDirectory() {
		return (item instanceof VFSContainer);
	}

	@Override
	public boolean isFile() {
		return (item instanceof VFSLeaf);
	}

	@Override
	public String getName() {
		return item.getName();
	}

	@Override
	public long getContentLength() {
		return (item instanceof VFSLeaf ? ((VFSLeaf)item).getSize() : null);
	}

	@Override
	public String getETag() {
	       if (weakETag == null) {
	            synchronized (this) {
	                if (weakETag == null) {
	                    long contentLength = getContentLength();
	                    long lastModified = getLastModified();
	                    if ((contentLength >= 0) || (lastModified >= 0)) {
	                        weakETag = "W/\"" + contentLength + "-" +
	                                   lastModified + "\"";
	                    }
	                }
	            }
	        }
	        return weakETag;
	}

	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public InputStream getInputStream() {
		return (item instanceof VFSLeaf ? ((VFSLeaf)item).getInputStream() : null);
	}

	@Override
	public long getCreation() {
        try {
        	if(item instanceof JavaIOItem) {
        		JavaIOItem ioItem = (JavaIOItem)item;
	            BasicFileAttributes attrs = Files.readAttributes(ioItem.getBasefile().toPath(),
	                    BasicFileAttributes.class);
	            return attrs.creationTime().toMillis();
        	}
        	return 0;
        } catch (IOException e) {
            log.warn("getCreationFail" + item, e);
            return 0;
        }
	}

	@Override
	public void increaseDownloadCount() {
		try {
			if (item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
				CoreSpringFactory.getImpl(VFSRepositoryService.class).increaseDownloadCount(item);
			}
		} catch (Exception e) {
			log.error("Cannot increase download counter: " + item, e);
		}
	}
}
