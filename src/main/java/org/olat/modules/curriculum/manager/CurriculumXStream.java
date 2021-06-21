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
package org.olat.modules.curriculum.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRef;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRefs;
import org.olat.modules.curriculum.model.CurriculumElementToTaxonomyLevelImpl;
import org.olat.modules.curriculum.model.CurriculumElementTypeImpl;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.portfolio.handler.BinderXStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 17 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumXStream {
	
	private static final Logger log = Tracing.createLoggerFor(BinderXStream.class);
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	
	static {
		Class<?>[] types = new Class[] {
				Curriculum.class, CurriculumImpl.class, CurriculumElement.class, CurriculumElementImpl.class,
				CurriculumElementType.class, CurriculumElementTypeImpl.class, CurriculumElementTypeManagedFlag.class,
				CurriculumLectures.class, CurriculumCalendars.class, CurriculumLearningProgress.class,
				CurriculumElementToTaxonomyLevel.class, CurriculumElementToTaxonomyLevelImpl.class,
				CurriculumElementToRepositoryEntryRef.class, CurriculumElementToRepositoryEntryRefs.class,
				Hashtable.class, HashMap.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.omitField(CurriculumImpl.class, "group");
		xstream.omitField(CurriculumImpl.class, "organisation");
		xstream.omitField(CurriculumElementImpl.class, "group");
		xstream.omitField(CurriculumElementImpl.class, "curriculumParent");
		xstream.omitField(CurriculumElementImpl.class, "taxonomyLevels");
	}
	
	public static final Curriculum curriculumFromPath(Path path)
	throws IOException {
		try(InputStream inStream = Files.newInputStream(path)) {
			return (Curriculum)xstream.fromXML(inStream);
		} catch (Exception e) {
			log.error("Cannot import this map: {}", path, e);
			return null;
		}
	}
	
	public static final CurriculumElementToRepositoryEntryRefs entryRefsFromPath(Path path)
	throws IOException {
		try(InputStream inStream = Files.newInputStream(path)) {
			return (CurriculumElementToRepositoryEntryRefs)xstream.fromXML(inStream);
		} catch (Exception e) {
			log.error("Cannot import this map: {}", path, e);
			return null;
		}
	}
	
	public static final Curriculum fromXml(String xml) {
		return (Curriculum)xstream.fromXML(xml);
	}
	
	public static final String toXml(Curriculum curriculum) {
		return xstream.toXML(curriculum);
	}
	
	public static final void toStream(Curriculum curriculum, ZipOutputStream zout)
	throws IOException {
		try(OutputStream out=new ShieldOutputStream(zout)) {
			xstream.toXML(curriculum, out);
		} catch (Exception e) {
			log.error("Cannot export this curriculum: {}", curriculum, e);
		}
	}
	
	public static final void toStream(CurriculumElementToRepositoryEntryRefs entryRefs, ZipOutputStream zout)
	throws IOException {
		try(OutputStream out=new ShieldOutputStream(zout)) {
			xstream.toXML(entryRefs, out);
		} catch (Exception e) {
			log.error("Cannot export these entries references: {}", entryRefs, e);
		}
	}
}
