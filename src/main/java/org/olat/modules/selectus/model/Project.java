/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

/**
 * 
 * Initial date: 10 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Project {
	
	public boolean hasData();
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public String getFinancialImpact1();
	
	public void setFinancialImpact1(String impact);
	
	public String getFinancialImpact2();
	
	public void setFinancialImpact2(String impact);
	
	public String getFinancialImpact3();
	
	public void setFinancialImpact3(String impact);
	
	public String getFinancialImpact4();
	
	public void setFinancialImpact4(String impact);
	
	public String getFinancialImpact5();
	
	public void setFinancialImpact5(String impact);
	
	public Date getStartDate();
	
	public void setStartDate(Date date);
	
	public String getDuration();
	
	public void setDuration(String duration);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getAcronym();

	public void setAcronym(String acronym);

	public String getKeywords();

	public void setKeywords(String keywords);

	public String getDisciplines();

	public void setDisciplines(String discipines);

}
