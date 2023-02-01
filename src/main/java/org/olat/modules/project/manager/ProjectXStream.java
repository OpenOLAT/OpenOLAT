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
package org.olat.modules.project.manager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjArtefactImpl;
import org.olat.modules.project.model.ProjFileImpl;
import org.olat.modules.project.model.ProjNoteImpl;
import org.olat.modules.project.model.ProjProjectImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * The XStream has its security features enabled.
 * 
 * Initial date: 16 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				ProjArtefactImpl.class,
				ProjFileImpl.class,
				ProjProjectImpl.class,
				ProjectStatus.class,
				ProjNoteImpl.class,
				VFSMetadataImpl.class,
				IdentityImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("Project", ProjProjectImpl.class);
		xstream.omitField(ProjProjectImpl.class, "baseGroup");
		
		xstream.alias("Artefact", ProjArtefactImpl.class);
		xstream.omitField(ProjArtefactImpl.class, "project");
		xstream.omitField(ProjArtefactImpl.class, "baseGroup");
		
		xstream.alias("File", ProjFileImpl.class);
		xstream.alias("Note", ProjNoteImpl.class);
		
		xstream.alias("Identity", IdentityImpl.class);
		xstream.omitField(IdentityImpl.class, "version");
		xstream.omitField(IdentityImpl.class, "creationDate");
		xstream.omitField(IdentityImpl.class, "lastLogin");
		xstream.omitField(IdentityImpl.class, "status");
		xstream.omitField(IdentityImpl.class, "user");
		
		xstream.alias("VFSMetadata", VFSMetadataImpl.class);
		xstream.omitField(VFSMetadataImpl.class, "parent");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}
	
	private static final XStream rolesXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {};
		rolesXStream.addPermission(new ExplicitTypePermission(types));
		rolesXStream.alias("Roles", List.class);
		rolesXStream.alias("Roles", ArrayList.class);
		rolesXStream.alias("Role", String.class);
	}
	
	public static String rolesToXml(Collection<String> roles) {
		return roles != null && !roles.isEmpty()
				? rolesXStream.toXML(new ArrayList<>(roles))
				: null;
	}
	
}
