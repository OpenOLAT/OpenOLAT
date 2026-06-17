/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.title_generator;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.PersonImpl;

/**
 * 
 * Initial date: 3 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UZHPhilSalutationGeneratorTest extends OlatTestCase {
	
	@Autowired
	private UZHPhilSalutationGenerator generator;
	
	@Test
	public void getFullname_prof_f() {
		Application app = createApplication("prof", "Eleanore", "Beauvoire", "f");
		String titleFullnameEn = generator.getFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getFullname(app, Locale.GERMAN);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getFullname_dr_o() {
		Application app = createApplication("dr", "Eleanore", "Beauvoire", "o");
		String titleFullnameEn = generator.getFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getFullname(app, Locale.GERMAN);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameDe);
	}
    
	@Test
	public void getTitleFullname_f() {
		Application app = createApplication("", "Eleanore", "Beauvoire", "f");
		String titleFullnameEn = generator.getTitleFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getTitleFullname(app, Locale.GERMAN);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getTitleFullname_o() {
		Application app = createApplication("", "Eleanore", "Beauvoire", "o");
		String titleFullnameEn = generator.getTitleFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getTitleFullname(app, Locale.GERMAN);
		Assert.assertEquals("Eleanore Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getTitleFullname_prof_f() {
		Application app = createApplication("prof", "Eleanore", "Beauvoire", "f");
		String titleFullnameEn = generator.getTitleFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Professor Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getTitleFullname(app, Locale.GERMAN);
		Assert.assertEquals("Professorin Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getTitleFullname_prof_o() {
		Application app = createApplication("prof", "Eleanore", "Beauvoire", "o");
		String titleFullnameEn = generator.getTitleFullname(app, Locale.ENGLISH);
		Assert.assertEquals("Professor Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getTitleFullname(app, Locale.GERMAN);
		Assert.assertEquals("Prof. Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getSalutation_f() {
		Application app = createApplication("", "Eleanore", "Beauvoire", "f");
		String titleFullnameEn = generator.getSalutation(app, Locale.ENGLISH);
		Assert.assertEquals("Dear Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getSalutation(app, Locale.GERMAN);
		Assert.assertEquals("Sehr geehrte Frau Beauvoire", titleFullnameDe);
	}
	
	@Test
	public void getSalutation_o() {
		Application app = createApplication("", "Eleanore", "Beauvoire", "o");
		String titleFullnameEn = generator.getSalutation(app, Locale.ENGLISH);
		Assert.assertEquals("Dear Eleanore Beauvoire", titleFullnameEn);
		String titleFullnameDe = generator.getSalutation(app, Locale.GERMAN);
		Assert.assertEquals("Guten Tag Eleanore Beauvoire", titleFullnameDe);
	}
	
	private Application createApplication(String title, String firstName, String lastName, String gender) {
		ApplicationImpl app = new ApplicationImpl();
		app.setPerson(new PersonImpl());
		app.getPerson().setTitle(title);
		app.getPerson().setFirstName(firstName);
		app.getPerson().setLastName(lastName);
		app.getPerson().setGender(gender);
		return app;
	}
		
	

}
