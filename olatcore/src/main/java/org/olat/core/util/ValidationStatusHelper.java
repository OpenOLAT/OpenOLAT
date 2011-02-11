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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ValidationStatusHelper
 * 
 * <P>
 * Initial Date:  04.12.2006 <br>
 * @author patrickb
 */
public class ValidationStatusHelper {

	public static ValidationStatus[] sort(List statusList) {
		Collections.sort(statusList, new ErrorsGtWarningGtInfo());
		/*
		 * remove NOERRORS size bigger 1; NOERROR -> Level == OFF > SEVERE > WARNING >
		 * INFO
		 */
		if (statusList.size() > 1) {
			for(int i=0;i<statusList.size();i++) {
				if(statusList.get(i)==ValidationStatus.NOERROR) {
					//aha found one to remove. Remove shifts all elements to the left, e.g. subtracts one of the indices
					statusList.remove(i);
					//thus we have to loop on place to remove all NOERRORS which shift to the ith place.
					while(statusList.get(i)==ValidationStatus.NOERROR) {
						statusList.remove(i);
					}
				}
			}
		}
		ValidationStatus[] retVal = new ValidationStatus[statusList.size()];
		retVal = (ValidationStatus[]) statusList.toArray(retVal);
		return retVal;
	}

	private static class ErrorsGtWarningGtInfo implements Comparator {
		public int compare(Object o1, Object o2) {
			ValidationStatus s1 = (ValidationStatus) o1;
			ValidationStatus s2 = (ValidationStatus) o2;
			return s2.getLevel().intValue() - s1.getLevel().intValue();
		}
	}

}
