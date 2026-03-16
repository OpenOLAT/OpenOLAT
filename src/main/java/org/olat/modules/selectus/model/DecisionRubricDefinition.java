/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DecisionRubricDefinition extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getRubric();
	
	public void setRubric(String rubric);

	public String getType();

	public void setType(String type);

	public boolean isSum();

	public void setSum(boolean sum);

	public int getWeight();

	public void setWeight(int weight);
	
	public int getPos();
	
	public void setPos(int position);

}
