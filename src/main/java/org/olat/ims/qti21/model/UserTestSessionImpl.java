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
package org.olat.ims.qti21.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.ims.qti21.UserTestSession;
import org.olat.repository.RepositoryEntry;

/**
 * This a custom implementation of CandidateSession
 * 
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qtitestsession")
@Table(name="o_qti_test_session")
@NamedQueries({
	@NamedQuery(name="loadTestSessionsByUserAndCourse", query="select session from qtitestsession session where session.courseEntry.key=:courseEntryKey and session.identity.key=:identityKey and session.courseSubIdent=:courseSubIdent")
	
})
public class UserTestSessionImpl implements UserTestSession, Persistable {

	private static final long serialVersionUID = -6069133323360142500L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
    private RepositoryEntry testEntry;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_course", nullable=true, insertable=true, updatable=false)
    private RepositoryEntry courseEntry;

    @Column(name="q_course_subident", nullable=true, insertable=true, updatable=false)
	private String courseSubIdent;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
    private Identity identity;

    /** Is this session running in author mode? (I.e. providing debugging information) */
    @Column(name="q_author_mode", nullable=false, insertable=true, updatable=true)
    private boolean authorMode;
    
    @Column(name="q_storage", nullable=false, insertable=true, updatable=false)
    private String storage;

    /**
     * Timestamp indicating when the session has been <strong>finished</strong>.
     * This is a QTIWorks specific concept with the following meaning:
     * <ul>
     *   <li>
     *     A test is marked as finished once the candidate gets to the end of the last
     *     enterable testPart. At this time, the outcome variables are finalised and will
     *     be sent back to the LTI TC (if appropriate). A test only finishes once.
     *   </li>
     *   <li>
     *     A standalone item is marked as finished once the item session ends. At this time,
     *     the outcome variables are sent back to the LTI TC (if appropriate). These variables
     *     are normally final, but it is currently possible for items to reopen. The session can
     *     finish again in this case.
     *   </li>
     * </ul>
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="q_finish_time", nullable=true, insertable=true, updatable=true)
    private Date finishTime;

    /**
     * Timestamp indicating when the session has been terminated. Session termination can
     * occur in two ways:
     * <ul>
     *   <li>When the candidate naturally exits the session</li>
     *   <li>When the instructor explicitly terminates the session</li>
     * </ul>
     * Once terminated, a session is no longer available to the candidate.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="q_termination_time", nullable=true, insertable=true, updatable=true)
    private Date terminationTime;

    /**
     * Flag to indicate if this session blew up while running, either because
     * the assessment was not runnable, or because of a logic error.
     */
    @Column(name="q_exploded", nullable=false, insertable=true, updatable=true)
    private boolean exploded;

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

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

    @Override
	public Date getLastModified() {
		return lastModified;
	}

    @Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public RepositoryEntry getTestEntry() {
		return testEntry;
	}

	public void setTestEntry(RepositoryEntry testEntry) {
		this.testEntry = testEntry;
	}

	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}

	public void setCourseEntry(RepositoryEntry courseEntry) {
		this.courseEntry = courseEntry;
	}

	public String getCourseSubIdent() {
		return courseSubIdent;
	}

	public void setCourseSubIdent(String courseSubIdent) {
		this.courseSubIdent = courseSubIdent;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public boolean isAuthorMode() {
		return authorMode;
	}

	public void setAuthorMode(boolean authorMode) {
		this.authorMode = authorMode;
	}

	public boolean isExploded() {
		return exploded;
	}

	public void setExploded(boolean exploded) {
		this.exploded = exploded;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Date getTerminationTime() {
		return terminationTime;
	}

	public void setTerminationTime(Date terminationTime) {
		this.terminationTime = terminationTime;
	}

	@Override
	public int hashCode() {
		return key == null ? -86534687 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserTestSessionImpl) {
			UserTestSessionImpl session = (UserTestSessionImpl)obj;
			return getKey() != null && getKey().equals(session.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
