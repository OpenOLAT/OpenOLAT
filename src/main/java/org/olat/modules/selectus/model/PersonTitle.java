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
 * Initial date: 16.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PersonTitle {
	
	Dr("Dr.", "edit.application.title.dr"),
	Prof("Prof.", "edit.application.title.prof"),
	ProfDr("Prof.Dr.", "edit.application.title.profdr"),
	PDDr("PD Dr.", "edit.application.title.pddr"),
	DrPhD("Dr./PhD", "edit.application.title.drphd"),
	MD("MD", "edit.application.title.md"),
	Mr("Mr.", "edit.application.title.mr"),
	Mrs("Mrs.", "edit.application.title.mrs"),
	Ms("Ms.", "edit.application.title.ms"),
	Pfr("Pfr.", "edit.application.title.pfr"),
	PfrDr("Pfr. Dr.", "edit.application.title.pfrdr"),
	ProfEmDr("Prof.em.Dr", "edit.application.title.profemdr"),
	DrMed("Dr med", "edit.application.title.drmed");
	
	private final String title;
	private final String i18nKey;
	
	private PersonTitle(String title, String i18nKey) {
		this.title = title;
		this.i18nKey = i18nKey;
	}
	
	public String title() {
		return title;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static boolean isTitle(String value) {
		String lcValue = value.toLowerCase();
		return (lcValue.contains("dr") || lcValue.contains("pr"));
	}

}
