/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
