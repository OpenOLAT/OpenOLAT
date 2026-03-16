/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.resources;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 26 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneratedListExcelResourceTest {
	
	@Test
	public void split() {
		String text = "Ceci est un long text a couper avec efficacite et sobriete mais ou sont passe les accents";
		
		List<String> chunks = GeneratedListExcelResource.split(text, 32);
		Assert.assertNotNull(chunks);
		Assert.assertEquals(3, chunks.size());
		Assert.assertEquals("Ceci est un long text a couper", chunks.get(0));
		Assert.assertEquals(" avec efficacite et sobriete", chunks.get(1));
		Assert.assertEquals(" mais ou sont passe les accents", chunks.get(2));
	}

}
