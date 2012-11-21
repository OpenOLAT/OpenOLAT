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

package org.olat.upgrade.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class BookmarkImpl extends PersistentObject {
	private static final long serialVersionUID = -7176191690095059290L;
	private static final int DISPLAYRESTYPE_MAXLENGTH = 50;
	private static final int OLATRESTYPE_MAXLENGTH = 50;

	// bookmark fields
	private String displayrestype; // resourceable type to display to user
	private String olatrestype; // olat resourceable type
	private Long olatreskey; // olat resourceable key

	private String title;
	private String description;
	private String detaildata;
	private Identity owner = null;

	/**
	 * Default constructor (needed by hibernate).
	 */
	protected BookmarkImpl() {
		super();
	}

	/**
	 * @param displayrestype
	 * @param olatrestype
	 * @param olatreskey
	 * @param title
	 * @param intref
	 * @param ident
	 */
	BookmarkImpl(String displayrestype, String olatrestype, Long olatreskey, String title, String intref, Identity ident) {
		super();
		this.displayrestype = displayrestype;
		this.olatrestype = olatrestype;
		this.olatreskey = olatreskey;
		this.title = title;
		this.detaildata = intref;
		this.owner = ident;
	}

	/**
	 * @see Bookmark#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#getDetaildata()
	 */
	public String getDetaildata() {
		return detaildata;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#getOlatreskey()
	 */
	public Long getOlatreskey() {
		return olatreskey;
	}

	/**
	 * @see Bookmark#getOlatrestype()
	 */
	public String getOlatrestype() {
		return olatrestype;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#getDisplayrestype()
	 */
	public String getDisplayrestype() {
		return displayrestype;
	}

	/**
	 * @see Bookmark#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setDescription(java.lang.String)
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setDetaildata(java.lang.String)
	 */
	public void setDetaildata(String string) {
		detaildata = string;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setOlatreskey(java.lang.Long)
	 */
	public void setOlatreskey(Long reskey) {
		olatreskey = reskey;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setOlatrestype(java.lang.String)
	 */
	public void setOlatrestype(String string) {
		if (string.length() > OLATRESTYPE_MAXLENGTH) throw new AssertException("olatrestype in o_bookmark too long.");
		olatrestype = string;
	}

	/**
	 * Set the res type to be displayed in the bookmark broperties.
	 * 
	 * @param string
	 */
	public void setDisplayrestype(String string) {
		if (string.length() > DISPLAYRESTYPE_MAXLENGTH) throw new AssertException("displayrestype in o_bookmark too long.");
		displayrestype = string;
	}

	/**
	 * @see Bookmark#getOwner()
	 */
	public Identity getOwner() {
		return owner;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setOwner(org.olat.core.id.Identity)
	 */
	public void setOwner(Identity ident) {
		owner = ident;
	}

	/**
	 * @see org.olat.bookmark.Bookmark#setTitle(java.lang.String)
	 */
	public void setTitle(String string) {
		title = string;
	}

}
