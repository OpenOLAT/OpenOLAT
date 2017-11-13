package org.olat.modules.taxonomy.model;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;

/**
 * 
 * Initial date: 13 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyInfos implements TaxonomyRef, CreateInfo {
	
	private Long key;
	private Date creationDate;
	
	private String identifier;
	private String displayName;
	private String description;
	
	private int numOfLevels;
	
	public TaxonomyInfos(Taxonomy taxonomy, int numOfLevels) {
		key = taxonomy.getKey();
		creationDate = taxonomy.getCreationDate();
		identifier = taxonomy.getIdentifier();
		displayName = taxonomy.getDisplayName();
		description = taxonomy.getDescription();
		this.numOfLevels = numOfLevels;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public int getNumOfLevels() {
		return numOfLevels;
	}
	
	

}
