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
package org.olat.core.util.vfs.lock;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.services.webdav.servlets.FastHttpDateFormat;
import org.olat.core.commons.services.webdav.servlets.WebDAVDispatcherImpl;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.commons.services.webdav.servlets.XMLWriter;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLockApplicationType;

/**
 * Holds a lock information.
 */
public class LockInfo {

	private String path = "/";
	private String type = "write";
    private String scope = "exclusive";
    private int depth = 0;
    private String owner = "";
    private List<String> tokens = new ArrayList<>();
    private long expiresAt = 0;
    private Date creationDate = new Date();
    
    private String appName;
    private boolean vfsLock;
    private boolean webdavLock;
    private boolean exclusiveLock;
    private boolean collaborationLock;
    
    private boolean locked;
    
    private final Long lockedBy;
    
    public LockInfo() {
    	locked = false;
    	lockedBy = null;
    }

    public LockInfo(Long lockedBy, VFSLockApplicationType type, String appName) {
    	locked = true;
    	this.lockedBy = lockedBy;
    	this.appName = appName;
    	vfsLock = type == VFSLockApplicationType.vfs;
    	webdavLock = type == VFSLockApplicationType.webdav;
    	exclusiveLock = type == VFSLockApplicationType.exclusive;
    	collaborationLock = type == VFSLockApplicationType.collaboration;
    }
    
    public LockInfo(Identity lockedBy, VFSLockApplicationType appType) {
    	this(lockedBy == null ? null : lockedBy.getKey(), appType, null);
    }
    
    public LockInfo(Identity lockedBy, VFSLockApplicationType type, String appName) {
    	this(lockedBy == null ? null : lockedBy.getKey(), type, appName);
    } 
    
    public boolean isLocked() {
    	return locked;
    }

    public Long getLockedBy() {
		return lockedBy;
	}

	public String getWebPath() {
		return path;
	}
	
	public void setWebResource(WebResource resource) {
		path = resource.getPath();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = (owner == null ? null : owner.trim().replace("<href>", "<D:href>").replace("</href>", "</D:href>"));
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}

	public boolean isWebDAVLock() {
		return webdavLock;
	}
	
	public String getAppName() {
		return appName;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setWebDAVLock(boolean webdavLock) {
		this.webdavLock = webdavLock;
	}

	public boolean isVfsLock() {
		return vfsLock;
	}

	public void setVfsLock(boolean vfsLock) {
		this.vfsLock = vfsLock;
	}

	public boolean isExclusiveLock() {
		return exclusiveLock;
	}

	public void setExclusiveLock(boolean exclusiveLock) {
		this.exclusiveLock = exclusiveLock;
	}

	public boolean isCollaborationLock() {
		return collaborationLock;
	}

	public void setCollaborationLock(boolean collaborationLock) {
		this.collaborationLock = collaborationLock;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}
	
	public synchronized int getTokensSize() {
		return tokens.size();
	}
	
	public synchronized List<String> getTokens() {
		return tokens;
	}
	
	public synchronized Iterator<String> tokens() {
		return new ArrayList<>(tokens).iterator();
	}
	
	public synchronized void addToken(String token) {
		if(!tokens.contains(token)) {
			tokens.add(token);
		}
	}
	
	public synchronized void removeToken(String token) {
		tokens.remove(token);
	}
	
	public synchronized void clearTokens() {
		tokens.clear();
	}

	/**
     * Return true if the lock has expired.
     */
    public boolean hasExpired() {
        return (System.currentTimeMillis() > getExpiresAt());
    }

    /**
     * Return true if the lock is exclusive.
     */
    public boolean isExclusive() {
        return (scope.equals("exclusive"));
    }


    /**
     * Get an XML representation of this lock token. This method will
     * append an XML fragment to the given XML writer.
     */
    public synchronized void toXML(XMLWriter generatedXML) {

        generatedXML.writeElement("D", "activelock", XMLWriter.OPENING);

        generatedXML.writeElement("D", "locktype", XMLWriter.OPENING);
        generatedXML.writeElement("D", type, XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "locktype", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "lockscope", XMLWriter.OPENING);
        generatedXML.writeElement("D", scope, XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "lockscope", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "depth", XMLWriter.OPENING);
        if (depth == WebDAVDispatcherImpl.maxDepth) {
            generatedXML.writeText("infinity");
        } else {
            generatedXML.writeText("0");
        }
        generatedXML.writeElement("D", "depth", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "owner", XMLWriter.OPENING);
        generatedXML.writeText(owner);
        generatedXML.writeElement("D", "owner", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "timeout", XMLWriter.OPENING);
        long timeout = (expiresAt - System.currentTimeMillis()) / 1000;
        generatedXML.writeText("Second-" + timeout);
        generatedXML.writeElement("D", "timeout", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "locktoken", XMLWriter.OPENING);
        for (String token: tokens) {
            generatedXML.writeElement("D", "href", XMLWriter.OPENING);
            generatedXML.writeText("opaquelocktoken:" + token);
            generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
        }
        generatedXML.writeElement("D", "locktoken", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "activelock", XMLWriter.CLOSING);

    }

	/**
     * Get a String representation of this lock token.
     */
    @Override
    public synchronized String toString() {
        StringBuilder result =  new StringBuilder("Type:");
        result.append(type);
        result.append("\nScope:");
        result.append(scope);
        result.append("\nDepth:");
        result.append(depth);
        result.append("\nOwner:");
        result.append(owner);
        result.append("\nExpiration:");
        result.append(FastHttpDateFormat.formatDate(expiresAt, null));
        for (String token:tokens) {
            result.append("\nToken:").append(token);
        }
        result.append("\n");
        return result.toString();
    }
}

