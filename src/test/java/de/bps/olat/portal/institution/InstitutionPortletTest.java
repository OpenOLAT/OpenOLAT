package de.bps.olat.portal.institution;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * After the move from edenlib, check if the mapping from XStream is correct
 * 
 * <P>
 * Initial Date:  2 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InstitutionPortletTest {
	

	@Test
	public void readConfiguration() throws IOException, URISyntaxException {
		XStream xstream = InstitutionPortlet.getInstitutionConfigXStream();
		InputStream input = InstitutionPortletTest.class.getResourceAsStream("olat_portals_institution.xml");
		InstitutionConfiguration obj = (InstitutionConfiguration)xstream.fromXML(input);

		assertEquals("Test-Uni", obj.institution.get(0).shortname);
		assertEquals("360448",  obj.institution.get(0).polymorphlink.get(0).defaultId);
	}

}
