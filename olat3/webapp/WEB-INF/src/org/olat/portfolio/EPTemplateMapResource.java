package org.olat.portfolio;

import java.io.File;

import org.olat.fileresource.types.FileResource;

/**
 * 
 * Description:<br>
 * Olat cannot import something else than files
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPTemplateMapResource extends FileResource  {
	
	public static final String TYPE_NAME = "EPStructuredMapTemplate";

	/**
	 * @param f
	 * @return True if is of type.
	 */
	public static boolean validate(File f) {
		if(f.isDirectory()) {
			//unzip directory
			return new File(f, "map.xml").exists();
		}
		return f.getName().toLowerCase().endsWith("map.xml"); 
	}
}
