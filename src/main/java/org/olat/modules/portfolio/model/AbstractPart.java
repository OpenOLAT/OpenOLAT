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
package org.olat.modules.portfolio.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.Flow;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;

/**
 * 
 * Initial date: 09.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfpagepart")
@Table(name="o_pf_page_part")
@DiscriminatorColumn
public class AbstractPart implements Persistable, ModifiedInfo, CreateInfo, PagePart {

	private static final long serialVersionUID = -3352479924240874583L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	/** Only used for order by */
	@GeneratedValue
	@Column(name="pos", insertable=false, updatable=false)//order hack
	private long pos;

	@Column(name="p_content", nullable=true, insertable=true, updatable=true)
	private String content;
	@Column(name="p_flow", nullable=true, insertable=true, updatable=true)
	private String flow;
	@Column(name="p_layout_options", nullable=true, insertable=true, updatable=true)
	private String layoutOptions;
	
	@ManyToOne(targetEntity=PageBodyImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_page_body_id", nullable=true, insertable=true, updatable=true)
	private PageBody body;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public String getId() {
		return key.toString();
	}
	
	@Override
	@Transient
	public String getType() {
		return "asbtract";
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

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	@Override
	public String getLayoutOptions() {
		return layoutOptions;
	}

	@Override
	public void setLayoutOptions(String options) {
		layoutOptions = options;
	}

	@Override
	public Flow getPartFlow() {
		return StringHelper.containsNonWhitespace(flow) ? Flow.valueOf(flow) : null;
	}

	@Override
	public void setPartFlow(Flow flow) {
		if(flow == null) {
			this.flow = null;
		} else {
			this.flow = flow.name();
		}
	}

	public PageBody getBody() {
		return body;
	}

	public void setBody(PageBody body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		return key == null ? 2396 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AbstractPart) {
			AbstractPart part = (AbstractPart)obj;
			return key != null && key.equals(part.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
