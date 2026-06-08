/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Locale;

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
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionXStream;

/**
 * 
 * Initial date: 3 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="posattributedefinition")
@Table(name="o_selectus_pos_attribute_def")
public class PositionAttributeDefinitionImpl implements PositionAttributeDefinition {

	private static final long serialVersionUID = 5345742930551278225L;

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
	
	@Column(name="orderpos", nullable=false, insertable=true, updatable=true)
	private Integer orderPosition;

	@Column(name="label", nullable=true, unique=false, insertable=true, updatable=true)
	private String label;
	@Column(name="labelde", nullable=true, unique=false, insertable=true, updatable=true)
	private String labelDe;
	@Column(name="labelfr", nullable=true, unique=false, insertable=true, updatable=true)
	private String labelFr;
	@Column(name="placeholder", nullable=true, unique=false, insertable=true, updatable=true)
	private String placeholder;
	@Column(name="placeholderde", nullable=true, unique=false, insertable=true, updatable=true)
	private String placeholderDe;
	@Column(name="placeholderfr", nullable=true, unique=false, insertable=true, updatable=true)
	private String placeholderFr;
	@Column(name="applicationtab", nullable=false, unique=false, insertable=true, updatable=true)
	private String tab;
	@Column(name="attributetype", nullable=true, unique=false, insertable=true, updatable=true)
	private String type;
	@Column(name="attributemandatory", nullable=false, unique=false, insertable=true, updatable=true)
	private boolean mandatory;
	@Column(name="attributeconfiguration", nullable=true, unique=false, insertable=true, updatable=true)
	private String attributeConfiguration;
	
	@ManyToOne(targetEntity=PositionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_position_id", nullable=true, insertable=true, updatable=true)
	private Position position;
	
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
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabelDe() {
		return labelDe;
	}

	public void setLabelDe(String labelDe) {
		this.labelDe = labelDe;
	}

	public String getLabelFr() {
		return labelFr;
	}

	public void setLabelFr(String labelFr) {
		this.labelFr = labelFr;
	}

	@Override
	public boolean useLabel(String val) {
		return (StringHelper.containsNonWhitespace(label) && label.equalsIgnoreCase(val)) 
				|| (StringHelper.containsNonWhitespace(labelDe) && labelDe.equalsIgnoreCase(val)) 
				|| (StringHelper.containsNonWhitespace(labelFr) && labelFr.equalsIgnoreCase(val));
	}

	@Override
	public String getLabel(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getLabelDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getLabelFr();
		}
		return getLabel();
	}
	
	@Override
	public String getLabel(Locale locale, boolean lenient) {
		String val = null;
		if(lenient) {
			if(locale != null) {
				if(locale.getLanguage().equals("de")) {
					val = getLabelDe();
				} else if(locale.getLanguage().equals("fr")) {
					val = getLabelFr();
				} else if(locale.getLanguage().equals("en")) {
					val = getLabel();
				}
			}
			
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getLabel();
			}
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getLabelDe();
			}
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getLabelFr();
			}
		} else {
			val = getLabel(locale);
		}
		return val;
	}

	@Override
	public void setLabel(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setLabelDe(text);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setLabelFr(text);
		} else {
			setLabel(text);
		}
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getPlaceholderDe() {
		return placeholderDe;
	}

	public void setPlaceholderDe(String placeholderDe) {
		this.placeholderDe = placeholderDe;
	}
	
	public String getPlaceholderFr() {
		return placeholderFr;
	}

	public void setPlaceholderFr(String placeholderFr) {
		this.placeholderFr = placeholderFr;
	}

	@Override
	public String getPlaceholder(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getPlaceholderDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getPlaceholderFr();
		}
		return getPlaceholder();
	}
	
	@Override
	public String getPlaceholder(Locale locale, boolean lenient) {
		String val = null;
		if(lenient) {
			if(locale != null) {
				if(locale.getLanguage().equals("de")) {
					val = getPlaceholderDe();
				} else if(locale.getLanguage().equals("fr")) {
					val = getPlaceholderFr();
				} else if(locale.getLanguage().equals("en")) {
					val = getPlaceholder();
				}
			}
			
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getPlaceholder();
			}
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getPlaceholderDe();
			}
			if(!StringHelper.containsNonWhitespace(val)) {
				val = getPlaceholderFr();
			}
		} else {
			val = getPlaceholder(locale);
		}
		return val;
	}
	
	@Override
	public void setPlaceholder(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setPlaceholderDe(text);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setPlaceholderFr(text);
		} else {
			setPlaceholder(text);
		}
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}
	
	@Override
	public PositionApplicationAttributeTabEnum getTabEnum() {
		return StringHelper.containsNonWhitespace(tab) ? PositionApplicationAttributeTabEnum.valueOf(tab) : null;
	}

	@Override
	public boolean isMandatory() {
		return mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	@Override
	public String getAttributeConfiguration() {
		return attributeConfiguration;
	}

	@Override
	public void setAttributeConfiguration(String attributeConfiguration) {
		this.attributeConfiguration = attributeConfiguration;
	}

	@Override
	public <T> T getConfiguration(Class<T> configurationClass) {
		return getAttributeConfiguration() == null ? null :
			PositionAttributeDefinitionXStream.fromXml(getAttributeConfiguration(), configurationClass);
	}

	@Override
	public void setConfiguration(Object configuration) {
		String xmlConfig = null;
		if(configuration != null) {
			xmlConfig = PositionAttributeDefinitionXStream.toXml(configuration);
		}
		setAttributeConfiguration(xmlConfig);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public PositionAttributeDefinitionTypeEnum getTypeEnum() {
		return type == null ? PositionAttributeDefinitionTypeEnum.question : PositionAttributeDefinitionTypeEnum.valueOf(type);
	}
	
	public void setTypeEnum(PositionAttributeDefinitionTypeEnum type) {
		if(type == null) {
			this.type = null;
		} else {
			this.type = type.name();
		}
	}

	@Override
	public Integer getOrderPosition() {
		return orderPosition;
	}

	@Override
	public void setOrderPosition(Integer orderPosition) {
		this.orderPosition = orderPosition;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 869857629 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionAttributeDefinitionImpl) {
			PositionAttributeDefinitionImpl def = (PositionAttributeDefinitionImpl)obj;
			return getKey() != null && getKey().equals(def.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
