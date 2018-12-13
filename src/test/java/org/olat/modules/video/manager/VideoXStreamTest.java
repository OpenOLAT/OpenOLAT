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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.model.VideoMarkersImpl;
import org.olat.modules.video.model.VideoQuestionImpl;
import org.olat.modules.video.model.VideoQuestionsImpl;

/**
 * 
 * Initial date: 28 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoXStreamTest {
	
	private static final OLog log = Tracing.createLoggerFor(VideoXStreamTest.class);
	
	@Test
	public void writeRead_markers() {
		
		byte[] content = null;
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			VideoMarkersImpl markers = new VideoMarkersImpl();
			VideoMarkerImpl marker = new VideoMarkerImpl();
			marker.setBegin(new Date());
			marker.setStyle("#000000");
			marker.setDuration(120);
			marker.setHeight(20.0d);
			marker.setWidth(22.0d);
			marker.setTop(24.0d);
			marker.setLeft(26.0d);
			marker.setId(UUID.randomUUID().toString());
			marker.setText("<p>hello world</p>");
			markers.getMarkers().add(marker);
			VideoXStream.toXml(out, markers);
			content = out.toByteArray();
		} catch(IOException e) {
			log.error("", e);
		}
		
		Assert.assertNotNull(content);
		
		try(ByteArrayInputStream in = new ByteArrayInputStream(content)) {
			VideoMarkers markers = VideoXStream.fromXml(in, VideoMarkers.class);
			Assert.assertNotNull(markers);
			Assert.assertEquals(1, markers.getMarkers().size());
			
			VideoMarker marker = markers.getMarkers().get(0);
			Assert.assertNotNull(marker);
			Assert.assertNotNull(marker.getBegin());
			Assert.assertEquals("#000000", marker.getStyle());
			Assert.assertEquals(120l, marker.getDuration());
			Assert.assertEquals(20.0d, marker.getHeight(), 0.00001d);
			Assert.assertEquals(22.0d, marker.getWidth(), 0.00001d);
			Assert.assertEquals(24.0d, marker.getTop(), 0.00001d);
			Assert.assertEquals(26.0d, marker.getLeft(), 0.00001d);
			Assert.assertNotNull(marker.getId());
			Assert.assertEquals("<p>hello world</p>", marker.getText());
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
	}
	
	@Test
	public void writeRead_questions() {
		
		byte[] content = null;
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			VideoQuestionsImpl questions = new VideoQuestionsImpl();
			VideoQuestionImpl question = new VideoQuestionImpl();
			question.setBegin(new Date());
			question.setId(UUID.randomUUID().toString());
			questions.getQuestions().add(question);
			VideoXStream.toXml(out, questions);
			content = out.toByteArray();
		} catch(IOException e) {
			log.error("", e);
		}
		
		Assert.assertNotNull(content);
		
		try(ByteArrayInputStream in = new ByteArrayInputStream(content)) {
			VideoQuestions questions = VideoXStream.fromXml(in, VideoQuestions.class);
			Assert.assertNotNull(questions);
			Assert.assertEquals(1, questions.getQuestions().size());
			
			VideoQuestion question = questions.getQuestions().get(0);
			Assert.assertNotNull(question);
			Assert.assertNotNull(question.getBegin());
			Assert.assertNotNull(question.getId());
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
	}
}
