/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 15.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum HighestDegreeType {

	bachelor("edit.application.degreetype.bachelor"),
	master("edit.application.degreetype.master"),
	md("edit.application.degreetype.md"),
	phd("edit.application.degreetype.phd"),
	dr("edit.application.degreetype.dr"),
	other("edit.application.degreetype.other"),
	pd("edit.application.degreetype.pd"),
	diplom("edit.application.degreetype.diplom"),
	prof("edit.application.degreetype.prof"),
	habilitation("edit.application.degreetype.habilitation"),
	drphd("edit.application.degreetype.drphd"),
	diplommaster("edit.application.degreetype.diplommaster"),
	drmed("edit.application.degreetype.drmed"),
	bacheloralt("edit.application.degreetype.bacheloralt"),
	drdes("edit.application.degreetype.drdes"),
	ma("edit.application.degreetype.ma"),
	msc("edit.application.degreetype.msc"),
	phddrexp("edit.application.degreetype.phddr.expected"),
	;
	
	private final String i18nKey;
	
	private HighestDegreeType(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String i18nKey() {
		return i18nKey;
	}

}
