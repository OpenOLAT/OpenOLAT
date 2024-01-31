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
package org.olat.modules.ceditor.manager;

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
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.jpa.AbstractPart;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.model.jpa.EvaluationFormPart;
import org.olat.modules.ceditor.model.jpa.HTMLPart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.PageBodyImpl;
import org.olat.modules.ceditor.model.jpa.PageImpl;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.SpacerPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.model.jpa.TitlePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.cemedia.model.MediaVersionMetadataImpl;

import com.microsoft.graph.callrecords.models.Media;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 24 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageXStream {
	
	private static final Logger log = Tracing.createLoggerFor(PageXStream.class);
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				PageImpl.class, Page.class, PageBodyImpl.class, PageBody.class,
				AbstractPart.class, PagePart.class, Media.class, MediaImpl.class,
				MediaVersion.class, MediaVersionImpl.class, MediaVersionMetadata.class, MediaVersionMetadataImpl.class,
				ContainerPart.class, EvaluationFormPart.class,  HTMLPart.class, MathPart.class,
				MediaPart.class, SpacerPart.class, TablePart.class, TitlePart.class, ParagraphPart.class,
				CodePart.class,
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.aliasType("page", PageImpl.class);
		xstream.omitField(PageImpl.class, "baseGroup");
		xstream.omitField(PageImpl.class, "previewPath");
		xstream.omitField(PageImpl.class, "previewMetadata");
		xstream.aliasType("pageBody", PageBodyImpl.class);
		xstream.aliasType("containerPart", ContainerPart.class);
		xstream.aliasType("evaluationFormPart", EvaluationFormPart.class);
		xstream.aliasType("htmlPart", HTMLPart.class);
		xstream.aliasType("mathPart", MathPart.class);
		xstream.aliasType("mediaPart", MediaPart.class);
		xstream.omitField(MediaPart.class, "identity");
		xstream.aliasType("spacerPart", SpacerPart.class);
		xstream.aliasType("tablePart", TablePart.class);
		xstream.aliasType("titlePart", TitlePart.class);
		xstream.aliasType("paragraphPart", ParagraphPart.class);
		xstream.omitField(AbstractPart.class, "body");
		xstream.aliasType("media", MediaImpl.class);
		xstream.omitField(MediaImpl.class, "author");
		xstream.omitField(MediaImpl.class, "versions");
		xstream.aliasType("mediaVersion", MediaVersionImpl.class);
		xstream.omitField(MediaVersionImpl.class, "metadata");
		
	}
	
	public static final Page fromPath(Path path)
	throws IOException {	
		try(InputStream inStream = Files.newInputStream(path)) {
			return (Page)xstream.fromXML(inStream);
		} catch (Exception e) {
			log.error("Cannot import this page: {}", path, e);
			return null;
		}
	}
	
	public static final Page fromStream(InputStream in)
	throws IOException {
		return (Page)xstream.fromXML(in);
	}
	
	public static final void toStream(Page page, ZipOutputStream zout)
	throws IOException {
		try(OutputStream out=new ShieldOutputStream(zout)) {
			xstream.toXML(page, out);
		} catch (Exception e) {
			log.error("Cannot export this page: {}", page, e);
		}
	}
	
	public static final void toStream(Page page, OutputStream out)
	throws IOException {
		xstream.toXML(page, out);
	}
	
	public static final String toXML(Page page)
	throws IOException {
		return xstream.toXML(page);
	}
}
