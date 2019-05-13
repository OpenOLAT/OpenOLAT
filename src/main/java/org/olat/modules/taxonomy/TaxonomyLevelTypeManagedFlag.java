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
package org.olat.modules.taxonomy;

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum TaxonomyLevelTypeManagedFlag {
	
	all,
	 identifier(all),
	 displayName(all),
	 description(all),
	 cssClass(all),
	 externalId(all),
	 visibility(all),
	 subTypes(all),
	 librarySettings(all),
	 copy(all),
	 delete(all);
	
	private TaxonomyLevelTypeManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(TaxonomyLevelTypeManagedFlag.class);
	public static final TaxonomyLevelTypeManagedFlag[] EMPTY_ARRAY = new TaxonomyLevelTypeManagedFlag[0];
	
	private static TaxonomyModule taxonomyModule;

	private TaxonomyLevelTypeManagedFlag() {
		//
	}
	
	private TaxonomyLevelTypeManagedFlag(TaxonomyLevelTypeManagedFlag... parents) {
		if(parents == null) {
			this.parents = new TaxonomyLevelTypeManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static TaxonomyLevelTypeManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			TaxonomyLevelTypeManagedFlag[] flagEnums = new TaxonomyLevelTypeManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						TaxonomyLevelTypeManagedFlag flagEnum = valueOf(flag);
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
	
	public static String toString(TaxonomyLevelTypeManagedFlag... flags) {
		StringBuilder sb = new StringBuilder();
		if(flags != null && flags.length > 0 && flags[0] != null) {
			for(TaxonomyLevelTypeManagedFlag flag:flags) {
				if(flag != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(flag.name());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static boolean isManaged(TaxonomyLevelType type, TaxonomyLevelTypeManagedFlag marker) {
		if(taxonomyModule == null) {
			taxonomyModule = CoreSpringFactory.getImpl(TaxonomyModule.class);
		}
		if(!taxonomyModule.isManagedTaxonomyLevels()) {
			return false;
		}
		
		if(type != null && (contains(type, marker) || contains(type, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(TaxonomyLevelTypeManagedFlag[] flags, TaxonomyLevelTypeManagedFlag marker) {
		if(taxonomyModule == null) {
			taxonomyModule = CoreSpringFactory.getImpl(TaxonomyModule.class);
		}
		if(!taxonomyModule.isManagedTaxonomyLevels()) {
			return false;
		}
		
		if(flags != null && (contains(flags, marker) || contains(flags, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(TaxonomyLevelType type, TaxonomyLevelTypeManagedFlag... markers) {
		if(type == null) return false;
		TaxonomyLevelTypeManagedFlag[] flags = type.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(TaxonomyLevelTypeManagedFlag[] flags, TaxonomyLevelTypeManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(TaxonomyLevelTypeManagedFlag flag:flags) {
			for(TaxonomyLevelTypeManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}