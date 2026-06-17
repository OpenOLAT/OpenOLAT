/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.olat.modules.selectus.DocumentEnum;

/**
 * 
 * Initial date: 30 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionImplTest {
	
	@Test
	public void getDocumentSizes() {
		Map<DocumentEnum, Integer> sizes = new HashMap<>();
		sizes.put(DocumentEnum.curriculumVitae, Integer.valueOf(6));
		sizes.put(DocumentEnum.coveringLetter, Integer.valueOf(8));
		
		PositionImpl position = new PositionImpl();
		position.setDocumentSizes(sizes);
	
		String stringufied = position.getDocSizes();
		Assert.assertNotNull(stringufied);
		
		Map<DocumentEnum,Integer> persistedSizes = position.getDocumentSizes();
		Assert.assertEquals(2, persistedSizes.size());
		Assert.assertTrue(persistedSizes.containsKey(DocumentEnum.curriculumVitae));
		Assert.assertTrue(persistedSizes.containsKey(DocumentEnum.coveringLetter));
		Assert.assertFalse(persistedSizes.containsKey(DocumentEnum.clinicalDisciplines));
		Assert.assertEquals(Integer.valueOf(6), persistedSizes.get(DocumentEnum.curriculumVitae));
		Assert.assertEquals(Integer.valueOf(8), persistedSizes.get(DocumentEnum.coveringLetter));
	}
	
	@Test
	public void getDocumentSizes_empty() {
		Map<DocumentEnum, Integer> sizes = new HashMap<>();
		PositionImpl position = new PositionImpl();
		position.setDocumentSizes(sizes);
	
		String stringufied = position.getDocSizes();
		Assert.assertNull(stringufied);
		
		Map<DocumentEnum,Integer> persistedSizes = position.getDocumentSizes();
		Assert.assertTrue(persistedSizes.isEmpty());
	}
	
	@Test
	public void getDocumentNames() {
		Map<DocumentEnum, String> names = new HashMap<>();
		names.put(DocumentEnum.curriculumVitae, "Not an other CV");
		names.put(DocumentEnum.coveringLetter, "Cover what?");
		
		PositionImpl position = new PositionImpl();
		position.setDocumentNames(names);
	
		String stringufied = position.getDocNames();
		Assert.assertNotNull(stringufied);
		
		Map<DocumentEnum,String> persistedNames = position.getDocumentNames();
		Assert.assertEquals(2, persistedNames.size());
		Assert.assertTrue(persistedNames.containsKey(DocumentEnum.curriculumVitae));
		Assert.assertTrue(persistedNames.containsKey(DocumentEnum.coveringLetter));
		Assert.assertFalse(persistedNames.containsKey(DocumentEnum.clinicalDisciplines));
		Assert.assertEquals("Not an other CV", persistedNames.get(DocumentEnum.curriculumVitae));
		Assert.assertEquals("Cover what?", persistedNames.get(DocumentEnum.coveringLetter));
	}
	
	@Test
	public void getDocumentNames_empty() {
		Map<DocumentEnum, String> names = new HashMap<>();
		PositionImpl position = new PositionImpl();
		position.setDocumentNames(names);
	
		String stringuified = position.getDocSizes();
		Assert.assertNull(stringuified);
		
		Map<DocumentEnum,String> persistedNames = position.getDocumentNames();
		Assert.assertTrue(persistedNames.isEmpty());
	}
}
