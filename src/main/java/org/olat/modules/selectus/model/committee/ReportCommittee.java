/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.committee;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 3 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReportCommittee extends Persistable, CreateInfo, ModifiedInfo {
	
	public String getRole();

	public void setRole(String role);

	public String getRatingsRights();

	public void setRatingsRights(String ratingsRights);

	public String getGender();

	public void setGender(String gender);

	public String getUserClassification();

	public void setUserClassification(String userClassification);
	
	
	public Integer getNumOfRatingsA();

	public void setNumOfRatingsA(Integer numOfRatingsA);

	public Integer getNumOfRatingsB();

	public void setNumOfRatingsB(Integer numOfRatingsB);

	public Integer getNumOfRatingsC();

	public void setNumOfRatingsC(Integer numOfRatingsC);

	public Integer getNumOfAbstentions();

	public void setNumOfAbstentions(Integer numOfAbstentions);
	
	
	public Position getPosition();

}
