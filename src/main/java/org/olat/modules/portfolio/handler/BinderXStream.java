package org.olat.modules.portfolio.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.portfolio.Binder;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 08.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderXStream {
	
	private static final OLog log = Tracing.createLoggerFor(BinderXStream.class);
	private static final XStream myStream = XStreamHelper.createXStreamInstanceForDBObjects();
	
	
	public static final byte[] toBytes(Binder binder)
	throws IOException {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(out);) {
			//prepare a zip
			
			zipOut.putNextEntry(new ZipEntry("binder.xml"));
			myStream.toXML(binder, zipOut);
			zipOut.closeEntry();
			zipOut.close();

			return out.toByteArray();
		} catch (IOException e) {
			log.error("Cannot export this map: " + binder, e);
			return null;
		}
	}

}
