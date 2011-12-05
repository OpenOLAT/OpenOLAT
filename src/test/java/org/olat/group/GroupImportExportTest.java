package org.olat.group;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

/**
 * 
 * Description:<br>
 * Check import/export from group with XStream (was made with edenlib)
 * 
 * <P>
 * Initial Date:  5 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupImportExportTest {
	
	@Test
	public void importLearningGroupTest() {
		InputStream input = GroupImportExportTest.class.getResourceAsStream("learninggroupexport.xml");
		GroupXStream xstream = new GroupXStream();
		OLATGroupExport export = xstream.fromXML(input);
		assertNotNull(export);
		assertNotNull(export.getAreas());
		assertNotNull(export.getAreas().getGroups());
		assertEquals(1, export.getAreas().getGroups().size());
		assertNotNull(export.getGroups());
		assertNotNull(export.getGroups().getGroups());
		assertEquals(2, export.getGroups().getGroups().size());
		
		assertEquals("Form Group 2", export.getGroups().getGroups().get(1).name);
		
		String output = xstream.toXML(export);
		assertNotNull(output);
	}
	
	
	@Test
	public void importRightGroupTest() {
		InputStream input = GroupImportExportTest.class.getResourceAsStream("rightgroupexport.xml");
		GroupXStream xstream = new GroupXStream();
		OLATGroupExport export = xstream.fromXML(input);
		assertNotNull(export);
		assertNotNull(export.getAreas());
		assertNotNull(export.getGroups());
		assertNotNull(export.getGroups().getGroups());
		assertEquals(2, export.getGroups().getGroups().size());
		
		assertEquals("Test Right 2", export.getGroups().getGroups().get(1).name);
		
		String output = xstream.toXML(export);
		assertNotNull(output);
	}
}
