package org.olat.ims.qti21;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentTestMarks extends CreateInfo, ModifiedInfo {

	public String getMarks();
	
	public void setMarks(String marks);
}
