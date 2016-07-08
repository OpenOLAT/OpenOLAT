package org.olat.modules.portfolio.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldOutputStream;
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
	
	
	
	public static final Binder copy(Binder binder) {
		String stringuified = myStream.toXML(binder);
		Binder copiedBinder = (Binder)myStream.fromXML(stringuified);
		return copiedBinder;
	}
	
	public static final Binder fromPath(Path path)
	throws IOException {	
		try(InputStream inStream = Files.newInputStream(path)) {
			return (Binder)myStream.fromXML(inStream);
		} catch (Exception e) {
			log.error("Cannot import this map: " + path, e);
			return null;
		}
	}
	
	public static final void toStream(Binder binder, ZipOutputStream zout)
	throws IOException {
		try {
			myStream.toXML(binder, new ShieldOutputStream(zout));
		} catch (Exception e) {
			log.error("Cannot export this map: " + binder, e);
		}
	}

}
