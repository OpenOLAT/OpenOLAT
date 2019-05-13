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
package org.olat.modules.curriculum;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CurriculumElementTypeManagedFlag {
	
	all,
	 identifier(all),
	 displayName(all),
	 cssClass(all),
	 description(all),
	 externalId(all),
	 calendars(all),
	 lectures(all),
	 subTypes(all),
	 copy(all),
	 delete(all);
	
	private CurriculumElementTypeManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(CurriculumElementTypeManagedFlag.class);
	public static final CurriculumElementTypeManagedFlag[] EMPTY_ARRAY = new CurriculumElementTypeManagedFlag[0];
	
	private static CurriculumModule curriculumModule;

	private CurriculumElementTypeManagedFlag() {
		//
	}
	
	private CurriculumElementTypeManagedFlag(CurriculumElementTypeManagedFlag... parents) {
		if(parents == null) {
			this.parents = new CurriculumElementTypeManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static CurriculumElementTypeManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			CurriculumElementTypeManagedFlag[] flagEnums = new CurriculumElementTypeManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						CurriculumElementTypeManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: " + flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		}
		return EMPTY_ARRAY;
	}
	
	public static String toString(CurriculumElementTypeManagedFlag... flags) {
		StringBuilder sb = new StringBuilder();
		if(flags != null && flags.length > 0 && flags[0] != null) {
			for(CurriculumElementTypeManagedFlag flag:flags) {
				if(flag != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(flag.name());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static boolean isManaged(CurriculumElementType elementType, CurriculumElementTypeManagedFlag marker) {
		if(curriculumModule == null) {
			curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		}
		if(!curriculumModule.isCurriculumManaged()) {
			return false;
		}
		return (elementType != null && (contains(elementType, marker) || contains(elementType, marker.parents)));
	}
	
	public static boolean isManaged(CurriculumElementTypeManagedFlag[] flags, CurriculumElementTypeManagedFlag marker) {
		if(curriculumModule == null) {
			curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		}
		if(!curriculumModule.isCurriculumManaged()) {
			return false;
		}
		
		return (flags != null && (contains(flags, marker) || contains(flags, marker.parents)));
	}
	
	private static boolean contains(CurriculumElementType elementType, CurriculumElementTypeManagedFlag... markers) {
		if(elementType == null) return false;
		CurriculumElementTypeManagedFlag[] flags = elementType.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(CurriculumElementTypeManagedFlag[] flags, CurriculumElementTypeManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(CurriculumElementTypeManagedFlag flag:flags) {
			for(CurriculumElementTypeManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}