/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;


import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.olat.test.OlatTestCase;

import org.olat.modules.selectus.model.PersonImpl;
import org.olat.modules.selectus.ui.title_generator.ETHSalutationGenerator;

/**
 * 
 * Initial date: 19.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SalutationAndTitleTest extends OlatTestCase {
	
	@Test
	public void testSalutationAndTitleParser() {
		PersonImpl person = new PersonImpl();
		
		//Mr.
		person.setTitleInternal("Mr.");
		assertEquals("Mr.", person.getTitle());
		
		//Miss
		person.setTitleInternal("Miss");
		assertEquals("Miss", person.getTitle());
		
		//Ms.
		person.setTitleInternal("Ms.");
		assertEquals("Ms.", person.getTitle());
		
		//Mrs
		person.setTitleInternal("Mrs.");
		assertEquals("Mrs.", person.getTitle());
		
		//Dr.
		person.setTitleInternal("Dr.");
		assertEquals("Dr.", person.getTitle());
		
		//Prof.
		person.setTitleInternal("Prof.");
		assertEquals("Prof.", person.getTitle());
		
		//Prof.Dr.
		person.setTitleInternal("Prof.Dr.");
		assertEquals("Prof.Dr.", person.getTitle());
		
	}
	
	@Test
	public void testSalutationWithTitleParser() {
		PersonImpl person = new PersonImpl();
		
		person.setTitleInternal("Mr.+Prof.");
		assertEquals("Prof.", person.getTitle());
		
		person.setTitleInternal("Mr.+Prof.Dr.");
		assertEquals("Prof.Dr.", person.getTitle());
		
		person.setTitleInternal("Miss+Prof.");
		assertEquals("Prof.", person.getTitle());
	}

	@Test
	public void testETHSalutationWithoutTitle() {
		PersonImpl person = new PersonImpl();
		person.setFirstName("Firstname");
		person.setLastName("Lastname");
		
		person.setTitleInternal("");
		assertEquals("", person.getTitle());
		
		person.setTitleInternal(null);
		assertEquals("", person.getTitle());
		
		ETHSalutationGenerator esg = new ETHSalutationGenerator();
		Locale en = Locale.ENGLISH;
		Locale de = Locale.GERMAN;
		assertEquals("Firstname Lastname", esg.getTitleLastName(person, en));
		assertEquals("Firstname Lastname", esg.getTitleLastName(person, de));
	}


}
