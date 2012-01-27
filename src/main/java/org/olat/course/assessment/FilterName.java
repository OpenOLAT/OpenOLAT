package org.olat.course.assessment;

import org.olat.core.gui.ShortName;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FilterName implements ShortName {
	
	private final String name;
	
	public FilterName(String name) {
		this.name = name;
	}

	@Override
	public String getShortName() {
		return name;
	}
	
	

}
