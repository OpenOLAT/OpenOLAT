/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
* <p>
*/
package de.bps.olat.modules.cl;

import java.text.Collator;
import java.util.Comparator;

/**
 * Description:<br>
 * TODO: thomasw Class Description for ChecklistComparator
 * 
 * <P>
 * Initial Date:  11.03.2010 <br>
 * @author thomasw
 */
public class CheckpointComparator implements Comparator {

	/**
	 * indicate the attribute to be compared:
	 * 1 - title
	 * 2 - Description
	 * 3 - Mode
	 */
	private int checkpointAttribute = 1;
	
	/**
	 * shows if sorting shall be ascending (true) or descending (false).
	 */
	private boolean asc = true;

	public CheckpointComparator(int attribute, boolean asc) {
		this.checkpointAttribute = attribute;
		this.asc = asc;
	}
	
	@Override
	public int compare(Object arg0, Object arg1) {
		Checkpoint cp1 = (Checkpoint) arg0;
		Checkpoint cp2 = (Checkpoint) arg1;
		String comp1 = "", comp2 = "";
		Collator collator = Collator.getInstance();
		switch (checkpointAttribute) {
			case 1: 
				comp1 = cp1.getTitle();
				comp2 = cp2.getTitle();
				break;
			case 2: 
				comp1 = cp1.getDescription();
				comp2 = cp2.getDescription();
				break;
			case 3: 
				comp1 = cp1.getMode();
				comp2 = cp2.getMode();
				break;	
		}
		if (asc) {
			return collator.compare(comp1, comp2);
		} else {
			return collator.compare(comp2, comp1);
		}
	}
}
