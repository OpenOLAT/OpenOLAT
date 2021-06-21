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
package org.olat.modules.video.manager;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadata;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.modules.video.model.VideoMetadataImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 13 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMetaXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				VideoMeta.class, VideoMetaImpl.class,
				VideoMetadata.class, VideoMetadataImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.ignoreUnknownElements();

		xstream.alias("videoMeta", VideoMetaImpl.class);
		xstream.omitField(VideoMetaImpl.class, "videoResource");
	}
	
	public static void toXml(VFSLeaf file, VideoMeta obj) {
		XStreamHelper.writeObject(xstream, file, obj);
	}
	
	public static void toXml(OutputStream out, VideoMeta obj) {
		XStreamHelper.writeObject(xstream, out, obj);
	}
	
	/**
	 * Your responsible to close the input stream.
	 * 
	 * @param in The input stream
	 * @return The video metadata
	 */
	public static VideoMeta fromXml(InputStream in) {
		return (VideoMeta)xstream.fromXML(in);
	}
	
	public static VideoMeta fromXml(VFSLeaf file) {
		return (VideoMeta)XStreamHelper.readObject(xstream, file);
	}

}
