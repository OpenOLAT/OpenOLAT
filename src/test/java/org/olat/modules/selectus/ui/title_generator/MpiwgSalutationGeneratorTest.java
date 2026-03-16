/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.title_generator;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.PersonImpl;


/**
 * 
 * Initial date: 25 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MpiwgSalutationGeneratorTest {
	
	private MpiwgSalutationGenerator salutationGenerator = new MpiwgSalutationGenerator();

	@Test
	public void getTitleFullname() {
		
		ApplicationImpl app = new ApplicationImpl();
		app.setPerson(new PersonImpl());
		app.getPerson().setFirstName("Eleanore");
		app.getPerson().setLastName("Beauvoire");
		
		String titleFullname_en = salutationGenerator.getTitleFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Eleanore Beauvoire", titleFullname_en);
		
		String titleFullname_de = salutationGenerator.getTitleFullname(app, Locale.GERMAN);
		Assert.assertEquals("Eleanore Beauvoire", titleFullname_de);
	}

}
