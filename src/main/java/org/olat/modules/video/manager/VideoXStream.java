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

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.model.VideoMarkersImpl;
import org.olat.modules.video.model.VideoQuestionImpl;
import org.olat.modules.video.model.VideoQuestionsImpl;
import org.olat.modules.video.model.VideoSegmentCategoryImpl;
import org.olat.modules.video.model.VideoSegmentImpl;
import org.olat.modules.video.model.VideoSegmentsImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * The XStream has its security features enabled.
 * 
 * Initial date: 5 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				VideoMarker.class, VideoMarkerImpl.class, VideoMarkers.class, VideoMarkersImpl.class,
				VideoSegment.class, VideoSegmentImpl.class, VideoSegments.class, VideoSegmentsImpl.class,
				VideoSegmentCategory.class, VideoSegmentCategoryImpl.class,
				VideoQuestion.class, VideoQuestionImpl.class, VideoQuestions.class, VideoQuestionsImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.ignoreUnknownElements();

		xstream.alias("marker", VideoMarkerImpl.class);
		xstream.alias("markers", VideoMarkers.class);

		xstream.alias("segment", VideoSegmentImpl.class);
		xstream.alias("segmentCategory", VideoSegmentCategoryImpl.class);
		xstream.alias("segments", VideoSegments.class);

		xstream.alias("question", VideoQuestionImpl.class);
		xstream.alias("questions", VideoQuestions.class);
	}
	
	public static void toXml(OutputStream out, Object obj) {
		xstream.toXML(obj, out);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(InputStream in, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(in);
		return (U)obj;
	}
}
