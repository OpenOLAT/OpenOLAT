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
package org.olat.core.commons.services.commentAndRating.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

/**
 * Description:<br>
 * Implemenation of the user comment class
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
@Entity(name="usercomment")
@Table(name="o_usercomment")
public class UserCommentImpl implements Persistable, CreateInfo, UserComment {

	private static final long serialVersionUID = -1396648859230164778L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="comment_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Column(name="version", nullable=false, insertable=true, updatable=false)
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="resname", nullable=false, insertable=true, updatable=false)
	private String resName;
	@Column(name="resid", nullable=false, insertable=true, updatable=false)
	private Long resId;
	@Column(name="ressubpath", nullable=true, insertable=true, updatable=false)
	private String resSubPath;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="creator_id", nullable=false, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=UserCommentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="parent_key", nullable=true, updatable=true)
	private UserComment parent;
	
	@Column(name="commenttext", nullable=false, insertable=true, updatable=true)
	private String comment;

	/**
	 * Default constructor for hibernate, don't use this!
	 */
	public UserCommentImpl() {
		// 
	}

	/**
	 * Package constructor to create a new user comment with the given arguments
	 * @param ores
	 * @param subpath
	 * @param creator
	 * @param comment
	 */
	public UserCommentImpl(OLATResourceable ores, String subpath, Identity creator, String comment) {
		this.creator = creator;
		this.comment = comment;
		this.resName = ores.getResourceableTypeName();
		this.resId = ores.getResourceableId();
		this.resSubPath = subpath;
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public Long getResId() {
		return this.resId;
	}

	@Override
	public String getResName() {
		return this.resName;
	}

	@Override
	public String getResSubPath() {
		return this.resSubPath;
	}

	@Override
	public UserComment getParent() {
		return parent;
	}
	
	/**
	 * Set the resource subpath
	 * @param resSubPath
	 */
	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	/**
	 * Set the resoruce name
	 * @param resName
	 */
	public void setResName(String resName) {
		this.resName = resName;
	}

	/**
	 * Set the resource ID
	 * @param resId
	 */
	public void setResId(Long resId) {
		this.resId = resId;
	}

	@Override
	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public void setComment(String commentText) {
		this.comment = commentText;
	}

	@Override
	public void setParent(UserComment parentComment) {
		this.parent = parentComment;
	}

	@Override
	public void setCreationDate(Date date) {
		this.creationDate = date;	
	}
	
	@Override
	public int hashCode() {
		return key == null ? -1356 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserCommentImpl) {
			UserCommentImpl q = (UserCommentImpl)obj;
			return key != null && key.equals(q.key);
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
