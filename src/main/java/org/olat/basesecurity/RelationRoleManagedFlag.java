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
package org.olat.basesecurity;

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RelationRoleManagedFlag {

	all,
	 name(all),
	 rights(all),
     delete(all);
	
	private static BaseSecurityModule securityModule;
	private RelationRoleManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(RelationRoleManagedFlag.class);
	public static final RelationRoleManagedFlag[] EMPTY_ARRAY = new RelationRoleManagedFlag[0];
	
	private RelationRoleManagedFlag() {
		//
	}
	
	private RelationRoleManagedFlag(RelationRoleManagedFlag... parents) {
		if(parents == null) {
			this.parents = new RelationRoleManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static RelationRoleManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			RelationRoleManagedFlag[] flagEnums = new RelationRoleManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						RelationRoleManagedFlag flagEnum = valueOf(flag);
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
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static String toString(RelationRoleManagedFlag[] flags) {
		if(flags == null || flags.length == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		for(RelationRoleManagedFlag flag:flags) {
			if(sb.length() > 0) sb.append(",");
			sb.append(flag.name());
		}
		return sb.toString();
	}
	
	public static boolean isManaged(RelationRole re, RelationRoleManagedFlag marker) {
		if(securityModule == null) {
			securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		}
		if(!securityModule.isRelationRoleManaged()) {
			return false;
		}
		return re != null && (contains(re, marker) || contains(re, marker.parents));
	}
	
	public static boolean isManaged(RelationRoleManagedFlag[] flags, RelationRoleManagedFlag marker) {
		if(securityModule == null) {
			securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		}
		if(!securityModule.isRelationRoleManaged()) {
			return false;
		}
		return flags != null && (contains(flags, marker) || contains(flags, marker.parents));
	}
	
	private static boolean contains(RelationRole re, RelationRoleManagedFlag... markers) {
		if(re == null) return false;
		RelationRoleManagedFlag[] flags = re.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(RelationRoleManagedFlag[] flags, RelationRoleManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(RelationRoleManagedFlag flag:flags) {
			for(RelationRoleManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}
