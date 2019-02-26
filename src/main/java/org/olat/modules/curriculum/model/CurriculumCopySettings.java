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
package org.olat.modules.curriculum.model;

/**
 * 
 * Initial date: 19 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumCopySettings {
	
	private boolean copyDates;
	private boolean copyTaxonomy;
	private CopyResources copyResources;
	
	public CurriculumCopySettings() {
		//
	}

	public boolean isCopyDates() {
		return copyDates;
	}

	public void setCopyDates(boolean copyDates) {
		this.copyDates = copyDates;
	}

	public CopyResources getCopyResources() {
		return copyResources;
	}

	public void setCopyResources(CopyResources copyResources) {
		this.copyResources = copyResources;
	}

	public boolean isCopyTaxonomy() {
		return copyTaxonomy;
	}

	public void setCopyTaxonomy(boolean copyTaxonomy) {
		this.copyTaxonomy = copyTaxonomy;
	}
	
	public enum CopyResources {
		dont,
		relation,
		resource;
		
		public static CopyResources valueOf(String val, CopyResources def) {
			if(val == null) return def;
			
			for(CopyResources value:values()) {
				if(val.equals(value.name())) {
					return value;
				}
			}
			return def;
		}
	}
}
