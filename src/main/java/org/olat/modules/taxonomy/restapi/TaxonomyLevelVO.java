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
package org.olat.modules.taxonomy.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * Initial date: 5 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "taxonomyLevelVO")
public class TaxonomyLevelVO {
	
	private Long key;
	private String identifier;
	private String displayName;
	private String description;
	private String externalId;
	
	private Long parentKey;
	private Long typeKey;
	
	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { 
			"all",
			 "identifier(all)",
			 "displayName(all)",
			 "description(all)",
			 "externalId(all)",
			 "sortOrder(all)",
			 "type(all)",
			 "competences(all)",
			   "manageCompetence(competences, all)",
			   "teachCompetence(competences, all)",
			   "haveCompetence(competences, all)",
			   "targetCompetence(competences, all)",
			 "move(all)",
			 "delete(all)"})
	private String managedFlags;
	
	public TaxonomyLevelVO() {
		//
	}
	
	public static TaxonomyLevelVO valueOf(TaxonomyLevel taxonomyLevel) {
		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, I18nModule.getDefaultLocale());
		
		TaxonomyLevelVO vo = new TaxonomyLevelVO();
		vo.setKey(taxonomyLevel.getKey());
		vo.setIdentifier(taxonomyLevel.getIdentifier());
		vo.setDisplayName(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, taxonomyLevel));
		vo.setDescription(TaxonomyUIFactory.translateDescription(taxonomyTranslator, taxonomyLevel));
		vo.setExternalId(taxonomyLevel.getExternalId());
		if(taxonomyLevel.getParent() != null) {
			vo.setParentKey(taxonomyLevel.getParent().getKey());
		}
		if(taxonomyLevel.getType() != null) {
			vo.setTypeKey(taxonomyLevel.getType().getKey());
		}
		vo.setManagedFlags(taxonomyLevel.getManagedFlagsString());
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public Long getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(Long typeKey) {
		this.typeKey = typeKey;
	}

	public Long getParentKey() {
		return parentKey;
	}

	public void setParentKey(Long parentKey) {
		this.parentKey = parentKey;
	}
}
