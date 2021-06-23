/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.portfolio.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.portfolio.model.export.AssignmentXML;
import org.olat.modules.portfolio.model.export.BinderXML;
import org.olat.modules.portfolio.model.export.SectionXML;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 08.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderXStream {
	
	private static final Logger log = Tracing.createLoggerFor(BinderXStream.class);
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				BinderXML.class, SectionXML.class, AssignmentXML.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.aliasType("binder", BinderXML.class);
		xstream.aliasType("section", SectionXML.class);
		xstream.aliasType("assignment", AssignmentXML.class);

	}
	
	public static final BinderXML fromPath(Path path)
	throws IOException {	
		try(InputStream inStream = Files.newInputStream(path)) {
			return (BinderXML)xstream.fromXML(inStream);
		} catch (Exception e) {
			log.error("Cannot import this map: {}", path, e);
			return null;
		}
	}
	
	public static final void toStream(BinderXML binder, ZipOutputStream zout)
	throws IOException {
		try(OutputStream out=new ShieldOutputStream(zout)) {
			xstream.toXML(binder, out);
		} catch (Exception e) {
			log.error("Cannot export this map: {}", binder, e);
		}
	}
	
	public static final String toXML(BinderXML binder)
	throws IOException {
		return xstream.toXML(binder);
	}
}
