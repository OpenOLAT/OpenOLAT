/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
