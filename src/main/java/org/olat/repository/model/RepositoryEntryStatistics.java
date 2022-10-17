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
package org.olat.repository.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="repoentrystats")
@Table(name="o_repositoryentry_stats")
public class RepositoryEntryStatistics implements Persistable {

	private static final long serialVersionUID = -464784571144089794L;

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
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="r_rating", nullable=true, insertable=true, updatable=true)
	private Double rating;
	@Column(name="r_num_of_ratings", nullable=true, insertable=true, updatable=true)
	private long numOfRatings;
	@Column(name="r_num_of_comments", nullable=true, insertable=true, updatable=true)
	private long numOfComments;
	
	@Column(name="r_launchcounter", nullable=false, insertable=true, updatable=true)
	private long launchCounter;
	@Column(name="r_downloadcounter", nullable=false, insertable=true, updatable=true)
	private long downloadCounter;
	@Column(name="r_lastusage", nullable=false, insertable=true, updatable=true)
	private Date lastUsage;
	
	
	public RepositoryEntryStatistics() {
		//
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}
	
	public long getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(long numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public long getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(long numOfComments) {
		this.numOfComments = numOfComments;
	}

	public long getLaunchCounter() {
		return launchCounter;
	}

	public void setLaunchCounter(long launchCounter) {
		this.launchCounter = launchCounter;
	}

	public long getDownloadCounter() {
		return downloadCounter;
	}

	public void setDownloadCounter(long downloadCounter) {
		this.downloadCounter = downloadCounter;
	}

	public Date getLastUsage() {
		return lastUsage;
	}

	public void setLastUsage(Date lastUsage) {
		this.lastUsage = lastUsage;
	}

	@Override
	public int hashCode() {
		return key == null ? 7530 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RepositoryEntryStatistics) {
			RepositoryEntryStatistics other = (RepositoryEntryStatistics) obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {	
		return equals(persistable);
	}
}
