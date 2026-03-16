/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 3 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PositionApplicationAttributeTabEnum {
	
	personalData,
	academicalBackground,
	project,
	custom1,
	custom2,
	custom3,
	custom4,
	global;
	
	
	public Tab tab() {
		switch(this) {
			case personalData: return Tab.personalData;
			case academicalBackground: return Tab.academicalBackground;
			case project: return Tab.project;
			case custom1: return Tab.custom1;
			case custom2: return Tab.custom2;
			case custom3: return Tab.custom3;
			case custom4: return Tab.custom4;
			default: return null;
		}
	}
}
