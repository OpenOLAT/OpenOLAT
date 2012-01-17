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

package org.olat.core.util.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.AttributeModificationException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributesException;
import javax.naming.directory.InvalidSearchControlsException;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoHelper;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.UserSession;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.VersionsManager;


/**
 * Filesystem Directory Context implementation helper class.
 * 
 * @author Remy Maucherat
 */

public class VFSDirContext extends BaseDirContext {

	//private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(VFSDirContext.class);

	// -------------------------------------------------------------- Constants

	/**
	 * The descriptive information string for this implementation.
	 */
	public static int bufferSize = 2048;
	
	
	private Identity identity;
	private UserSession userSession;

	// ----------------------------------------------------------- Constructors

	/**
	 * Builds a file directory context using the given environment.
	 */
	public VFSDirContext() {
		super();
	}

	/**
	 * Builds a file directory context using the given environment.
	 */
	public VFSDirContext(Hashtable env) {
		super(env);
	}

	// ----------------------------------------------------- Instance Variables

	/**
	 * The document base directory.
	 */
	protected VFSItem base = null;

	/**
	 * Absolute normalized filename of the base.
	 */
	protected String absoluteBase = null;

	/**
	 * Case sensitivity.
	 */
	protected boolean caseSensitive = true;

	/**
	 * Allow linking.
	 */
	protected boolean allowLinking = false;

	// ------------------------------------------------------------- Properties

	
	
	/**
	 * Set the document root.
	 * 
	 * @param vfsItem The new document root
	 * @exception IllegalArgumentException if the specified value is not supported
	 *              by this implementation
	 */
	public void setVirtualDocBase(VFSItem vfsItem) {
			base = vfsItem;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public VFSItem getVirtualDocBase() {
		return base;
	}
	
	/**
	 * Set the document root.
	 * 
	 * @param docBase The new document root
	 * @exception IllegalArgumentException if the specified value is not supported
	 *              by this implementation
	 * @exception IllegalArgumentException if this would create a malformed URL
	 */
	public void setDocBase(String docBase) {

		// disabled for VFSDirContext implementation...
		throw new IllegalArgumentException("setDocBase(String) not supported by VFSDirCOntext.");
	}

	/**
	 * Set case sensitivity.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Is case sensitive ?
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Set allow linking.
	 */
	public void setAllowLinking(boolean allowLinking) {
		this.allowLinking = allowLinking;
	}

	/**
	 * Is linking allowed.
	 */
	public boolean getAllowLinking() {
		return allowLinking;
	}

	public void setBuffer(int bytes) {
		bufferSize = bytes;
	}
	// --------------------------------------------------------- Public Methods

	/**
	 * Release any resources allocated for this directory context.
	 */
	public void release() {

		caseSensitive = true;
		allowLinking = false;
		absoluteBase = null;
		base = null;
		super.release();

	}

	// -------------------------------------------------------- Context Methods

	/**
	 * Retrieves the named object.
	 * 
	 * @param name the name of the object to look up
	 * @return the object bound to name
	 * @exception NamingException if a naming exception is encountered
	 */
	public Object lookup(String name) throws NamingException {
		
		VFSItem item = resolveFile(name);
		if (item == null) throw new NamingException("Resources not found: " + name);

		if (item instanceof VFSContainer) {
			VFSDirContext tempContext = new VFSDirContext(env);
			tempContext.setVirtualDocBase(item);
			return tempContext;
		} else {
			return new VFSResource(item);
		}
	}

	/**
	 * Check if resource can be copied (delegation to VFS item)
	 * @param name
	 * @return true: can copy; false: can not copy
	 */
	public boolean canCopy(String name) {
		VFSItem item = resolveFile(name);
		if (item != null && VFSConstants.YES.equals(item.canCopy())) return true;
		else return false;
	}

	/**
	 * Check if resource can be written (delegation to VFS item)
	 * @param name
	 * @return true: can write; false: can not write
	 */
	public boolean canWrite(String name) {
		// resolve item if it already exists
		VFSItem item = resolveFile(name);
		if (item == null) {
			// try to resolve parent in case the item does not yet exist
			int lastSlash = name.lastIndexOf("/");
			if (lastSlash > 0) {
				String containerName = name.substring(0, lastSlash);
				item = resolveFile(containerName);
			}
		}
		if (item == null) return false;
		
		VFSStatus status;
		if (item instanceof VFSContainer) {
			status = item.canWrite();
		} else {
			// read/write is not defined on item level, only on directory level
			status = item.getParentContainer().canWrite();
		}
		return VFSConstants.YES.equals(status);
	}

	/**
	 * Check if resource can be deleted (delegation to VFS item)
	 * @param name
	 * @return true: can delete; false: can not delete
	 */
	public boolean canDelete(String name) {
		VFSItem item = resolveFile(name);
		if (item != null && VFSConstants.YES.equals(item.canDelete())) {
			return !MetaInfoHelper.isLocked(item, userSession);
		}
		else return false;
	}

	/**
	 * Check if resource can be renamed (delegation to VFS item)
	 * @param name
	 * @return true: can rename; false: can not rename
	 */
	public boolean canRename(String name) {
		VFSItem item = resolveFile(name);
		if (item != null && VFSConstants.YES.equals(item.canRename())) {
			return !MetaInfoHelper.isLocked(item, userSession);
		}
		else return false;
	}	
	
	/**
	 * Unbinds the named object. Removes the terminal atomic name in name from the
	 * target context--that named by all but the terminal atomic part of name.
	 * <p>
	 * This method is idempotent. It succeeds even if the terminal atomic name is
	 * not bound in the target context, but throws NameNotFoundException if any of
	 * the intermediate contexts do not exist.
	 * 
	 * @param name the name to bind; may not be empty
	 * @exception NameNotFoundException if an intermediate context does not exist
	 * @exception NamingException if a naming exception is encountered
	 */
	public void unbind(String name) throws NamingException {
		VFSItem item = resolveFile(name);
		if (item == null) {
			throw new NamingException("Resources not found" + name);
		}

		if (item != null && VFSConstants.YES.equals(item.canDelete())) {
			if(MetaInfoHelper.isLocked(item, userSession)) {
				throw new NamingException("File locked: " + name);
			}
		}
		
		VFSStatus status = item.delete();
		if (status == VFSConstants.NO) {
			throw new NamingException("resources unbind failed" + name);
		}
	}

	/**
	 * Binds a new name to the object bound to an old name, and unbinds the old
	 * name. Both names are relative to this context. Any attributes associated
	 * with the old name become associated with the new name. Intermediate
	 * contexts of the old name are not changed.
	 * 
	 * @param oldName the name of the existing binding; may not be empty
	 * @param newName the name of the new binding; may not be empty
	 * @exception NameAlreadyBoundException if newName is already bound
	 * @exception NamingException if a naming exception is encountered
	 */
	public void rename(String oldName, String newName) throws NamingException {

		VFSItem oldFile = resolveFile(oldName);
		if (oldFile == null) {
			throw new NamingException("Resources not found: " + oldName);
		}
		if(MetaInfoHelper.isLocked(oldFile, userSession)) {
			throw new NoPermissionException("Locked");
		}

		VFSItem newFile = resolveFile(newName);
		if (newFile != null) {
			throw new NameAlreadyBoundException();
		}
		
		VFSStatus status = oldFile.rename(newName);
		if (status == VFSConstants.NO) {
			throw new NameAlreadyBoundException();
		}
	}

	/**
	 * Enumerates the names bound in the named context, along with the class names
	 * of objects bound to them. The contents of any subcontexts are not included.
	 * <p>
	 * If a binding is added to or removed from this context, its effect on an
	 * enumeration previously returned is undefined.
	 * 
	 * @param name the name of the context to list
	 * @return an enumeration of the names and class names of the bindings in this
	 *         context. Each element of the enumeration is of type NameClassPair.
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {

		VFSItem file = resolveFile(name);
		if (file == null) throw new NamingException("Resources not found: " + name);
		return new NamingContextEnumeration(list(file).iterator());

	}

	/**
	 * Enumerates the names bound in the named context, along with the objects
	 * bound to them. The contents of any subcontexts are not included.
	 * <p>
	 * If a binding is added to or removed from this context, its effect on an
	 * enumeration previously returned is undefined.
	 * 
	 * @param name the name of the context to list
	 * @return an enumeration of the bindings in this context. Each element of the
	 *         enumeration is of type Binding.
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		return new NamingContextEnumeration2(list(name));
	}

	/**
	 * Destroys the named context and removes it from the namespace. Any
	 * attributes associated with the name are also removed. Intermediate contexts
	 * are not destroyed.
	 * <p>
	 * This method is idempotent. It succeeds even if the terminal atomic name is
	 * not bound in the target context, but throws NameNotFoundException if any of
	 * the intermediate contexts do not exist. In a federated naming system, a
	 * context from one naming system may be bound to a name in another. One can
	 * subsequently look up and perform operations on the foreign context using a
	 * composite name. However, an attempt destroy the context using this
	 * composite name will fail with NotContextException, because the foreign
	 * context is not a "subcontext" of the context in which it is bound. Instead,
	 * use unbind() to remove the binding of the foreign context. Destroying the
	 * foreign context requires that the destroySubcontext() be performed on a
	 * context from the foreign context's "native" naming system.
	 * 
	 * @param name the name of the context to be destroyed; may not be empty
	 * @exception NameNotFoundException if an intermediate context does not exist
	 * @exception NotContextException if the name is bound but does not name a
	 *              context, or does not name a context of the appropriate type
	 */
	public void destroySubcontext(String name) throws NamingException {
		unbind(name);
	}

	/**
	 * Retrieves the named object, following links except for the terminal atomic
	 * component of the name. If the object bound to name is not a link, returns
	 * the object itself.
	 * 
	 * @param name the name of the object to look up
	 * @return the object bound to name, not following the terminal link (if any).
	 * @exception NamingException if a naming exception is encountered
	 */
	public Object lookupLink(String name) throws NamingException {
		// Note : Links are not supported
		return lookup(name);
	}

	/**
	 * Retrieves the full name of this context within its own namespace.
	 * <p>
	 * Many naming services have a notion of a "full name" for objects in their
	 * respective namespaces. For example, an LDAP entry has a distinguished name,
	 * and a DNS record has a fully qualified name. This method allows the client
	 * application to retrieve this name. The string returned by this method is
	 * not a JNDI composite name and should not be passed directly to context
	 * methods. In naming systems for which the notion of full name does not make
	 * sense, OperationNotSupportedException is thrown.
	 * 
	 * @return this context's name in its own namespace; never null
	 * @exception OperationNotSupportedException if the naming system does not
	 *              have the notion of a full name
	 * @exception NamingException if a naming exception is encountered
	 */
	public String getNameInNamespace() {
		return docBase;
	}

	// ----------------------------------------------------- DirContext Methods

	/**
	 * Retrieves selected attributes associated with a named object. See the class
	 * description regarding attribute models, attribute type names, and
	 * operational attributes.
	 * 
	 * @return the requested attributes; never null
	 * @param name the name of the object from which to retrieve attributes
	 * @param attrIds the identifiers of the attributes to retrieve. null
	 *          indicates that all attributes should be retrieved; an empty array
	 *          indicates that none should be retrieved
	 * @exception NamingException if a naming exception is encountered
	 */
	public Attributes getAttributes(String name, String[] attrIds) throws NamingException {

		// Building attribute list
		VFSItem file = resolveFile(name);
		if (file == null) throw new NamingException("Resources not found" + name);
		return new VFSResourceAttributes(file);

	}

	/**
	 * Modifies the attributes associated with a named object. The order of the
	 * modifications is not specified. Where possible, the modifications are
	 * performed atomically.
	 * 
	 * @param name the name of the object whose attributes will be updated
	 * @param mod_op the modification operation, one of: ADD_ATTRIBUTE,
	 *          REPLACE_ATTRIBUTE, REMOVE_ATTRIBUTE
	 * @param attrs the attributes to be used for the modification; may not be
	 *          null
	 * @exception AttributeModificationException if the modification cannot be
	 *              completed successfully
	 * @exception NamingException if a naming exception is encountered
	 */
	public void modifyAttributes(String name, int mod_op, Attributes attrs) {
		// not implemented
	}

	/**
	 * Modifies the attributes associated with a named object using an an ordered
	 * list of modifications. The modifications are performed in the order
	 * specified. Each modification specifies a modification operation code and an
	 * attribute on which to operate. Where possible, the modifications are
	 * performed atomically.
	 * 
	 * @param name the name of the object whose attributes will be updated
	 * @param mods an ordered sequence of modifications to be performed; may not
	 *          be null
	 * @exception AttributeModificationException if the modification cannot be
	 *              completed successfully
	 * @exception NamingException if a naming exception is encountered
	 */
	public void modifyAttributes(String name, ModificationItem[] mods) {
		// not implemented
	}

	/**
	 * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
	 */
	public void bind(String name, Object obj) throws NamingException {
		bind(name, obj, null);
	}
	
	/**
	 * Binds a name to an object, along with associated attributes. If attrs is
	 * null, the resulting binding will have the attributes associated with obj if
	 * obj is a DirContext, and no attributes otherwise. If attrs is non-null, the
	 * resulting binding will have attrs as its attributes; any attributes
	 * associated with obj are ignored.
	 * 
	 * @param name the name to bind; may not be empty
	 * @param obj the object to bind; possibly null
	 * @param attrs the attributes to associate with the binding
	 * @exception NameAlreadyBoundException if name is already bound
	 * @exception InvalidAttributesException if some "mandatory" attributes of the
	 *              binding are not supplied
	 * @exception NamingException if a naming exception is encountered
	 */
	public void bind(String name, Object obj, Attributes attrs) throws NamingException {

		// Note: No custom attributes allowed
		VFSItem file = resolveFile(name);
		if (file != null) throw new NameAlreadyBoundException("Resources already bound" + name);
		
		int lastSlash = name.lastIndexOf('/');
		if (lastSlash == -1) throw new NamingException();
		String parent = name.substring(0, lastSlash);
		VFSItem folder = resolveFile(parent);
		
		if (folder == null || (!(folder instanceof VFSContainer)))
			throw new NamingException("Resources bind failed: " + name);
		String newName = name.substring(lastSlash + 1);
		VFSLeaf childLeaf = ((VFSContainer)folder).createChildLeaf(newName);
		if (childLeaf == null)
			throw new NamingException("Resources bind failed: " + name);
		copyVFS(childLeaf, name, obj, attrs);
		
		VFSSecurityCallback callback = folder.getLocalSecurityCallback();
		if(callback != null && callback.getSubscriptionContext() != null) {
			SubscriptionContext subContext = callback.getSubscriptionContext();
			NotificationsManager.getInstance().markPublisherNews(subContext, null);
		}
		
		if(childLeaf instanceof MetaTagged) {
			MetaInfo infos = ((MetaTagged)childLeaf).getMetaInfo();
			if(infos != null && infos.getAuthorIdentity() == null) {
				infos.setAuthor(userSession.getIdentity().getName());
				infos.clearThumbnails();
				infos.write();
			}
		}
		
		//used by move operations
		if(obj instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)obj;
			if(vfsResource.vfsItem instanceof Versionable
					&& ((Versionable)vfsResource.vfsItem).getVersions().isVersioned()) {
				VFSLeaf currentVersion = (VFSLeaf)vfsResource.vfsItem;
				VersionsManager.getInstance().move(currentVersion, childLeaf, identity);
			}
		}
	}

	/**
	 * Binds a name to an object, along with associated attributes, overwriting
	 * any existing binding. If attrs is null and obj is a DirContext, the
	 * attributes from obj are used. If attrs is null and obj is not a DirContext,
	 * any existing attributes associated with the object already bound in the
	 * directory remain unchanged. If attrs is non-null, any existing attributes
	 * associated with the object already bound in the directory are removed and
	 * attrs is associated with the named object. If obj is a DirContext and attrs
	 * is non-null, the attributes of obj are ignored.
	 * 
	 * @param name the name to bind; may not be empty
	 * @param obj the object to bind; possibly null
	 * @param attrs the attributes to associate with the binding
	 * @exception InvalidAttributesException if some "mandatory" attributes of the
	 *              binding are not supplied
	 * @exception NamingException if a naming exception is encountered
	 */
	public void rebind(String name, Object obj, Attributes attrs) throws NamingException {

		// Note: No custom attributes allowed
		// Check obj type

		VFSItem vfsItem = resolveFile(name);
		if (vfsItem == null || (!(vfsItem instanceof VFSLeaf))) {
			throw new NamingException("Resources bind failed" + name);
		}
		if(MetaInfoHelper.isLocked(vfsItem, userSession)) {
			throw new NoPermissionException("Locked");
		}

		VFSLeaf file = (VFSLeaf)vfsItem;
		if(file instanceof Versionable && ((Versionable)file).getVersions().isVersioned()) {
			if(file.getSize() == 0) {
				VersionsManager.getInstance().createVersionsFor(file, true);
			} else {
				VersionsManager.getInstance().addToRevisions((Versionable)file, identity, "");
			}
		}
		
		copyVFS(file, name, obj, attrs);
		if(file instanceof MetaTagged) {
			MetaInfo infos = ((MetaTagged)file).getMetaInfo();
			if(infos != null && infos.getAuthorIdentity() == null) {
				infos.setAuthor(userSession.getIdentity().getName());
				infos.clearThumbnails();
				infos.write();
			}
		}
		
		//used by move operations
		if(obj instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)obj;
			if(vfsResource.vfsItem instanceof Versionable
					&& ((Versionable)vfsResource.vfsItem).getVersions().isVersioned()) {
				Versionable currentVersion = (Versionable)vfsResource.vfsItem;
				VersionsManager.getInstance().move(currentVersion, file.getParentContainer());
			}
		}
	}
	
	private void copyVFS(VFSLeaf file, String name, Object obj, Attributes attrs) throws NamingException {
		InputStream is = null;
		if (obj instanceof Resource) {
			try {
				is = ((Resource) obj).streamContent();
			} catch (IOException e) {
				// ignore, check further
			}
		} else if (obj instanceof InputStream) {
			is = (InputStream) obj;
		} else if (obj instanceof DirContext) {
			createSubcontext(name, attrs);
			return;
		}
		if (is == null) {
			throw new NamingException("Resources bind failed: " + name);
		}

		// Try to get Quota
		long quotaLeft = -1;
		boolean withQuotaCheck = false;
		VFSContainer parentContainer = file.getParentContainer();
		if (parentContainer != null) {
			quotaLeft = VFSManager.getQuotaLeftKB(parentContainer);
			if (quotaLeft != Quota.UNLIMITED) {
				quotaLeft = quotaLeft * 1024; // convert from kB
				withQuotaCheck = true;
			} else {
				withQuotaCheck = false;
			}
		}
		// Open os
		OutputStream os = null;
		byte buffer[] = new byte[bufferSize];
		int len = -1;
		try {
			os = file.getOutputStream(false);
			while (true) {
				len = is.read(buffer);
				if (len == -1) break;
				if (withQuotaCheck) {
					// re-calculate quota and check
					quotaLeft = quotaLeft - len;
					if (quotaLeft < 0) throw new NamingException("Quota exceeded.");
				}
				os.write(buffer, 0, len);
			}
		} catch (Exception e) {
			FileUtils.closeSafely(os); // close first, in order to be able to delete any reamins of the file
			file.delete();
			if (e instanceof NamingException) {
				throw (NamingException)e;
			}
			throw new NamingException("Resources bind failed");
		} finally {
			FileUtils.closeSafely(os);
			FileUtils.closeSafely(is);
		}
	}

	/**
	 * @see javax.naming.Context#createSubcontext(java.lang.String)
	 */
	public Context createSubcontext(String name) throws NamingException {
		return createSubcontext(name, null);
	}
	
	/**
	 * Creates and binds a new context, along with associated attributes. This
	 * method creates a new subcontext with the given name, binds it in the target
	 * context (that named by all but terminal atomic component of the name), and
	 * associates the supplied attributes with the newly created object. All
	 * intermediate and target contexts must already exist. If attrs is null, this
	 * method is equivalent to Context.createSubcontext().
	 * 
	 * @param name the name of the context to create; may not be empty
	 * @param attrs the attributes to associate with the newly created context
	 * @return the newly created context
	 * @exception NameAlreadyBoundException if the name is already bound
	 * @exception InvalidAttributesException if attrs does not contain all the
	 *              mandatory attributes required for creation
	 * @exception NamingException if a naming exception is encountered
	 */
	public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {

		VFSItem file = resolveFile(name);
		if (file != null) throw new NameAlreadyBoundException("Resources already bound" + name);
		
		int lastSlash = name.lastIndexOf('/');
		if (lastSlash == -1) throw new NamingException();
		String parent = name.substring(0, lastSlash);
		VFSItem folder = resolveFile(parent);
		if (folder == null || (!(folder instanceof VFSContainer)))
			throw new NamingException("Resources bind failed" + name);
		String newName = name.substring(lastSlash + 1);
		VFSItem childContainer = ((VFSContainer)folder).createChildContainer(newName);
		if (childContainer == null)
			throw new NamingException("Resources bind failed" + name);
		return (DirContext)lookup(name);

	}

	/**
	 * Retrieves the schema associated with the named object. The schema describes
	 * rules regarding the structure of the namespace and the attributes stored
	 * within it. The schema specifies what types of objects can be added to the
	 * directory and where they can be added; what mandatory and optional
	 * attributes an object can have. The range of support for schemas is
	 * directory-specific.
	 * 
	 * @param name the name of the object whose schema is to be retrieved
	 * @return the schema associated with the context; never null
	 * @exception OperationNotSupportedException if schema not supported
	 * @exception NamingException if a naming exception is encountered
	 */
	public DirContext getSchema(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/**
	 * Retrieves a context containing the schema objects of the named object's
	 * class definitions.
	 * 
	 * @param name the name of the object whose object class definition is to be
	 *          retrieved
	 * @return the DirContext containing the named object's class definitions;
	 *         never null
	 * @exception OperationNotSupportedException if schema not supported
	 * @exception NamingException if a naming exception is encountered
	 */
	public DirContext getSchemaClassDefinition(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/**
	 * Searches in a single context for objects that contain a specified set of
	 * attributes, and retrieves selected attributes. The search is performed
	 * using the default SearchControls settings.
	 * 
	 * @param name the name of the context to search
	 * @param matchingAttributes the attributes to search for. If empty or null,
	 *          all objects in the target context are returned.
	 * @param attributesToReturn the attributes to return. null indicates that all
	 *          attributes are to be returned; an empty array indicates that none
	 *          are to be returned.
	 * @return a non-null enumeration of SearchResult objects. Each SearchResult
	 *         contains the attributes identified by attributesToReturn and the
	 *         name of the corresponding object, named relative to the context
	 *         named by name.
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) {
		return null;
	}

	/**
	 * Searches in a single context for objects that contain a specified set of
	 * attributes. This method returns all the attributes of such objects. It is
	 * equivalent to supplying null as the atributesToReturn parameter to the
	 * method search(Name, Attributes, String[]).
	 * 
	 * @param name the name of the context to search
	 * @param matchingAttributes the attributes to search for. If empty or null,
	 *          all objects in the target context are returned.
	 * @return a non-null enumeration of SearchResult objects. Each SearchResult
	 *         contains the attributes identified by attributesToReturn and the
	 *         name of the corresponding object, named relative to the context
	 *         named by name.
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) {
		return null;
	}

	/**
	 * Searches in the named context or object for entries that satisfy the given
	 * search filter. Performs the search as specified by the search controls.
	 * 
	 * @param name the name of the context or object to search
	 * @param filter the filter expression to use for the search; may not be null
	 * @param cons the search controls that control the search. If null, the
	 *          default search controls are used (equivalent to (new
	 *          SearchControls())).
	 * @return an enumeration of SearchResults of the objects that satisfy the
	 *         filter; never null
	 * @exception InvalidSearchFilterException if the search filter specified is
	 *              not supported or understood by the underlying directory
	 * @exception InvalidSearchControlsException if the search controls contain
	 *              invalid settings
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) {
		return null;
	}

	/**
	 * Searches in the named context or object for entries that satisfy the given
	 * search filter. Performs the search as specified by the search controls.
	 * 
	 * @param name the name of the context or object to search
	 * @param filterExpr the filter expression to use for the search. The
	 *          expression may contain variables of the form "{i}" where i is a
	 *          nonnegative integer. May not be null.
	 * @param filterArgs the array of arguments to substitute for the variables in
	 *          filterExpr. The value of filterArgs[i] will replace each
	 *          occurrence of "{i}". If null, equivalent to an empty array.
	 * @param cons the search controls that control the search. If null, the
	 *          default search controls are used (equivalent to (new
	 *          SearchControls())).
	 * @return an enumeration of SearchResults of the objects that satisy the
	 *         filter; never null
	 * @exception ArrayIndexOutOfBoundsException if filterExpr contains {i}
	 *              expressions where i is outside the bounds of the array
	 *              filterArgs
	 * @exception InvalidSearchControlsException if cons contains invalid settings
	 * @exception InvalidSearchFilterException if filterExpr with filterArgs
	 *              represents an invalid search filter
	 * @exception NamingException if a naming exception is encountered
	 */
	public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons) {
		return null;
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Resolve a file relative to this base.
	 * Make sure, paths are relative
	 */
	protected VFSItem resolveFile(String name) {
		if (name == null) name = "";
		if (name.length() > 0 && name.charAt(0) == '/') name = name.substring(1);
		return base.resolve(name);
	}
	
	/**
	 * List the resources which are members of a collection.
	 * 
	 * @param file Collection
	 * @return Vector containg NamingEntry objects
	 */
	protected List<NamingEntry> list(VFSItem file) {

		List<NamingEntry> entries = new ArrayList<NamingEntry>();

		if (!(file instanceof VFSContainer)) return entries;
		List<VFSItem> children = ((VFSContainer)file).getItems();

		NamingEntry entry = null;
		for (VFSItem currentFile:children) {
			Object object;
			if (currentFile instanceof VFSContainer) {
				VFSDirContext tempContext = new VFSDirContext(env);
				tempContext.setVirtualDocBase(currentFile);
				object = tempContext;
			} else {
				object = new VFSResource(currentFile);
			}
			entry = new NamingEntry(currentFile.getName(), object, NamingEntry.ENTRY);
			entries.add(entry);
		}
		return entries;
	}

	// ----------------------------------------------- FileResource Inner Class

	/**
	 * This specialized resource implementation avoids opening the IputStream to
	 * the file right away (which would put a lock on the file).
	 */
	protected class VFSResource extends Resource {

		// -------------------------------------------------------- Constructor

		public VFSResource(VFSItem fileObject) {
			this.vfsItem = fileObject;
		}

		// --------------------------------------------------- Member Variables

		/**
		 * Associated file object.
		 */
		protected VFSItem vfsItem;

		/**
		 * File length.
		 */
		protected long length = -1L;

		// --------------------------------------------------- Resource Methods

		/**
		 * Content accessor.
		 * 
		 * @return InputStream
		 */
		public InputStream streamContent() throws IOException {
			if (!(vfsItem instanceof VFSLeaf)) throw new IOException("Can't get stream for VFSItem.");
			VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
			return vfsLeaf.getInputStream();
		}

	}

	// ------------------------------------- FileResourceAttributes Inner Class

	/**
	 * This specialized resource attribute implementation does some lazy reading
	 * (to speed up simple checks, like checking the last modified date).
	 */
	protected class VFSResourceAttributes extends ResourceAttributes {
		
		private static final long serialVersionUID = 4144775626100809634L;

		// -------------------------------------------------------- Constructor

		public VFSResourceAttributes(VFSItem vfsItem) {
			this.vfsItem = vfsItem;
		}

		// --------------------------------------------------- Member Variables

		protected VFSItem vfsItem;

		protected boolean accessed = false;
		
		protected String canonicalPath = null;

		// ----------------------------------------- ResourceAttributes Methods

		/**
		 * Is collection.
		 */
		public boolean isCollection() {
			if (!accessed) {
				collection = (vfsItem instanceof VFSContainer);
			}
			return collection;
		}

		/**
		 * Get content length.
		 * 
		 * @return content length value
		 */
		public long getContentLength() {
			if (contentLength != -1L) return contentLength;
			if (isCollection()) contentLength = 0;
			else {
				VFSLeaf leaf = (VFSLeaf)vfsItem;
				contentLength = leaf.getSize();
			}
			return contentLength;
		}

		/**
		 * Get creation time.
		 * 
		 * @return creation time value
		 */
		public long getCreation() {
			if (creation != -1L) return creation;
			if (isCollection()) creation = 0;
			else {
				creation = vfsItem.getLastModified();
			}
			return creation;
		}

		/**
		 * Get creation date.
		 * 
		 * @return Creation date value
		 */
		public Date getCreationDate() {
			return new Date(getCreation());
		}

		/**
		 * Get last modified time.
		 * 
		 * @return lastModified time value
		 */
		public long getLastModified() {
			if (lastModified != -1L) return lastModified;
			if (isCollection()) lastModified = 0;
			else {
				lastModified = vfsItem.getLastModified();
			}
			return lastModified;
		}

		/**
		 * Get lastModified date.
		 * 
		 * @return LastModified date value
		 */
		public Date getLastModifiedDate() {
			return new Date(getLastModified());
		}

		/**
		 * Get name.
		 * 
		 * @return Name value
		 */
		public String getName() {
			if (name == null) name = vfsItem.getName();
			return name;
		}

		/**
		 * Get resource type.
		 * 
		 * @return String resource type
		 */
		public String getResourceType() {
			if (!accessed) isCollection(); // needed to initialize
			return super.getResourceType();
		}

	}

	/**
	 * Represents a binding in a NamingContext.
	 * 
	 * @author Remy Maucherat
	 */

	public class NamingEntry {

		// --------------------------------------------------------------
		// Constants

		public static final int ENTRY = 0;
		public static final int LINK_REF = 1;
		public static final int REFERENCE = 2;

		public static final int CONTEXT = 10;

		// -----------------------------------------------------------
		// Constructors

		public NamingEntry(String name, Object value, int type) {
			this.name = name;
			this.value = value;
			this.type = type;
		}

		// ----------------------------------------------------- Instance Variables

		/**
		 * The type instance variable is used to avoid unsing RTTI when doing
		 * lookups.
		 */
		public int type;
		public String name;
		public Object value;

		// --------------------------------------------------------- Object Methods

		public boolean equals(Object obj) {
			if ((obj != null) && (obj instanceof NamingEntry)) {
				return name.equals(((NamingEntry) obj).name);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return name.hashCode();
		}

	}

	/**
	 * Naming enumeration implementation.
	 * 
	 * @author Remy Maucherat
	 */

	public class NamingContextEnumeration implements NamingEnumeration<NameClassPair> {

		// ----------------------------------------------------------- Constructors

		public NamingContextEnumeration(Iterator<NamingEntry> entries) {
			iterator = entries;
		}

		// -------------------------------------------------------------- Variables

		/**
		 * Underlying enumeration.
		 */
		protected Iterator<NamingEntry> iterator;

		// --------------------------------------------------------- Public Methods

		/**
		 * Retrieves the next element in the enumeration.
		 */
		public NameClassPair next() {
			return nextElement();
		}

		/**
		 * Determines whether there are any more elements in the enumeration.
		 */
		public boolean hasMore() {
			return iterator.hasNext();
		}

		/**
		 * Closes this enumeration.
		 */
		public void close() {
			//nothing to do
		}

		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		public NameClassPair nextElement() {
			NamingEntry entry = iterator.next();
			return new NameClassPair(entry.name, entry.value.getClass().getName());
		}
	}
	
	public class NamingContextEnumeration2 implements NamingEnumeration<Binding> {

		private final NamingEnumeration<NameClassPair> entries;
		
		public NamingContextEnumeration2(NamingEnumeration<NameClassPair> entries) {
			this.entries = entries;
		}

		public void close() {
			//nothing to do
		}

		public boolean hasMore() throws NamingException {
			return entries.hasMore();
		}

		public Binding next() throws NamingException {
			NameClassPair pair = entries.next();
			return new Binding(pair.getName(), pair.getClassName(), null);
		}

		public boolean hasMoreElements() {
			return entries.hasMoreElements();
		}

		public Binding nextElement() {
			try {
				return next();
			} catch (NamingException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
