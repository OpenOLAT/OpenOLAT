/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.model;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModule;

/**
 * 
 * Initial date: 11.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum AssessmentModeManagedFlag {
	
	all,
		general(all),
			name(all, general),
			description(all, general),
			begin(all, general),
			leadTime(all, general),
			end(all, general),
			followupTime(all, general),
			mode(all, general),
		restrictions(all),
			elements(all, restrictions),
			startElements(all, restrictions),
		access(all),
			ips(all,access),
			participants(all,access),
			coaches(all,access),
		safeexambrowser(all),
    	copy(all),
    	delete(all);
	

	private AssessmentModeManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeManagedFlag.class);
	public static final AssessmentModeManagedFlag[] EMPTY_ARRAY = new AssessmentModeManagedFlag[0];

	private static AssessmentModule assessmentModule;
	
	private AssessmentModeManagedFlag() {
		//
	}
	
	private AssessmentModeManagedFlag(AssessmentModeManagedFlag... parents) {
		if(parents == null) {
			this.parents = new AssessmentModeManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static AssessmentModeManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			AssessmentModeManagedFlag[] flagEnums = new AssessmentModeManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						AssessmentModeManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: {}", flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static boolean isManaged(AssessmentMode mode, AssessmentModeManagedFlag marker) {
		if(assessmentModule == null) {
			assessmentModule = CoreSpringFactory.getImpl(AssessmentModule.class);
		}
		if(!assessmentModule.isManagedAssessmentModes()) {
			return false;
		}
		
		if(mode != null && (contains(mode, marker) || contains(mode, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(AssessmentModeManagedFlag[] flags, AssessmentModeManagedFlag marker) {
		if(assessmentModule == null) {
			assessmentModule = CoreSpringFactory.getImpl(AssessmentModule.class);
		}
		if(!assessmentModule.isManagedAssessmentModes()) {
			return false;
		}
		
		if(flags != null && (contains(flags, marker) || contains(flags, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(AssessmentMode mode, AssessmentModeManagedFlag... markers) {
		if(mode == null) return false;
		AssessmentModeManagedFlag[] flags = mode.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(AssessmentModeManagedFlag[] flags, AssessmentModeManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(AssessmentModeManagedFlag flag:flags) {
			for(AssessmentModeManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}