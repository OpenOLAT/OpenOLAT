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
 * Initial date: 16 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum TaxonomyLevelManagedFlag {
	
	all,
	 identifier(all),
	 displayName(all),
	 description(all),
	 externalId(all),
	 sortOrder(all),
	 type(all),
	 competences(all),
	   manageCompetence(competences, all),
	   teachCompetence(competences, all),
	   haveCompetence(competences, all),
	   targetCompetence(competences, all),
	 move(all),
	 delete(all);
	
	private TaxonomyLevelManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(TaxonomyLevelManagedFlag.class);
	public static final TaxonomyLevelManagedFlag[] EMPTY_ARRAY = new TaxonomyLevelManagedFlag[0];
	
	private static TaxonomyModule taxonomyModule;

	private TaxonomyLevelManagedFlag() {
		//
	}
	
	private TaxonomyLevelManagedFlag(TaxonomyLevelManagedFlag... parents) {
		if(parents == null) {
			this.parents = new TaxonomyLevelManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static TaxonomyLevelManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			TaxonomyLevelManagedFlag[] flagEnums = new TaxonomyLevelManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						TaxonomyLevelManagedFlag flagEnum = valueOf(flag);
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
	
	public static String toString(TaxonomyLevelManagedFlag... flags) {
		StringBuilder sb = new StringBuilder();
		if(flags != null && flags.length > 0 && flags[0] != null) {
			for(TaxonomyLevelManagedFlag flag:flags) {
				if(flag != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(flag.name());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static boolean isManaged(TaxonomyLevel level, TaxonomyLevelManagedFlag marker) {
		if(taxonomyModule == null) {
			taxonomyModule = CoreSpringFactory.getImpl(TaxonomyModule.class);
		}
		if(!taxonomyModule.isManagedTaxonomyLevels()) {
			return false;
		}
		
		if(level != null && (contains(level, marker) || contains(level, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(TaxonomyLevelManagedFlag[] flags, TaxonomyLevelManagedFlag marker) {
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
	
	public static TaxonomyLevelManagedFlag getCorrespondingFlag(TaxonomyCompetenceTypes competenceType) {
		switch(competenceType) {
			case target: return TaxonomyLevelManagedFlag.targetCompetence;
			case have: return TaxonomyLevelManagedFlag.haveCompetence;
			case teach: return TaxonomyLevelManagedFlag.teachCompetence;
			case manage: return TaxonomyLevelManagedFlag.manageCompetence;
			default: return null;
		}
	}
	
	private static boolean contains(TaxonomyLevel level, TaxonomyLevelManagedFlag... markers) {
		if(level == null) return false;
		TaxonomyLevelManagedFlag[] flags = level.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(TaxonomyLevelManagedFlag[] flags, TaxonomyLevelManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(TaxonomyLevelManagedFlag flag:flags) {
			for(TaxonomyLevelManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}