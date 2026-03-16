/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.attributes;

import java.util.Comparator;

import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * Initial date: 31 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAttributeDefinitionComparator implements Comparator<PositionAttributeDefinition> {

	@Override
	public int compare(PositionAttributeDefinition o1, PositionAttributeDefinition o2) {
		int c = 0;
		if(o1 == null || o2 == null) {
			c = compareNulls(o1,  o2);
		} else  {
			Integer pos1 = o1.getOrderPosition();
			Integer pos2 = o2.getOrderPosition();
			
			if(pos1 == null || pos2 == null) {
				c = compareNulls(pos1,  pos2);
			} else {
				c = Integer.compare(pos1.intValue(), pos2.intValue());
			}
			
			if(c == 0) {
				Long k1 = o1.getKey();
				Long k2 = o2.getKey();
				
				if(k1 == null || k2 == null) {
					c = compareNulls(k1,  k2);
				} else {
					c = k1.compareTo(k2);
				}
			}
		}
		return c;
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		return o1 == null ? -1 : 1;
	}
}