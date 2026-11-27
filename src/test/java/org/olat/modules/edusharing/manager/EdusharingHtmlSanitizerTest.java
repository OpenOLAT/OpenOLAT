/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.edusharing.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

/**
 *
 * Initial date: 27.11.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingHtmlSanitizerTest {
	
	private final EdusharingHtmlSanitizer sut = new EdusharingHtmlSanitizer(
			"https://edu-sharing.example.org/edu-sharing/", List.of("www.youtube-nocookie.com"));
			
	@Test
	public void shouldRemoveVcardEventHandler() throws Exception {
		String html = sut.sanitize(load("render_es10_vcard_handler.html"));
		
		assertThat(html).contains("Eve").doesNotContain("onmouseover=").doesNotContain("<script");
	}
	
	@Test
	public void shouldRemoveOverlayStyle() {
		String html = sut.sanitize("<li data-value-key='Eve' style='position:fixed;top:0;left:0;width:100vw;z-index:99999'>Eve</li>");
		
		assertThat(html).doesNotContain("position").doesNotContain("z-index").doesNotContain("vw").contains("Eve");
	}
	
	@Test
	public void shouldRemoveForeignClassesAndIds() {
		String html = sut.sanitize("<li id='o_main' class='mdsValue modal-backdrop in o_hidden'>Eve</li>");
		
		assertThat(html).contains("class=\"mdsValue\"").doesNotContain("modal-backdrop").doesNotContain("o_hidden").doesNotContain("o_main");
	}
	
	@Test
	public void shouldRemoveNegativeValues() {
		String html = sut.sanitize("<div style='margin-top:-400px;width:300px'><a href='https://edu-sharing.example.org/x'>Fake</a></div>");
		
		assertThat(html).doesNotContain("-400px").contains("width:300px").contains("Fake");
	}
	
	@Test
	public void shouldKeepAudio() throws Exception {
		String html = sut.sanitize(load("render_es10_audio.html"));
		
		assertThat(html).contains("oo10079-audio.wav").doesNotContain("<script");
	}
	
	@Test
	public void shouldKeepVimeoIframeOfAllowedHost() {
		EdusharingHtmlSanitizer vimeo = new EdusharingHtmlSanitizer("https://edu-sharing.example.org/edu-sharing/", List.of("player.vimeo.com"));
		String html = vimeo.sanitize("<iframe src=\"//player.vimeo.com/video/76979871\" allowfullscreen></iframe>");
		
		assertThat(html).contains("player.vimeo.com/video/76979871").contains("sandbox=");
	}
	
	@Test
	public void shouldRemoveIframeWithoutSrc() {
		String html = sut.sanitize("<iframe title=\"src\"></iframe>");
		
		assertThat(html).doesNotContain("<iframe");
	}
	
	@Test
	public void shouldRemoveScriptStyleAndLink() throws Exception {
		String html = sut.sanitize(load("render_es10_image.html"));
		
		assertThat(html)
				.doesNotContain("<script")
				.doesNotContain("<style")
				.doesNotContain("<link")
				.contains("<img")
				.contains("src=\"https://edu-sharing.example.org:443/esrender/");
	}
	
	@Test
	public void shouldKeepH5pIframeWithSandbox() throws Exception {
		String html = sut.sanitize(load("render_es10_h5p.html"));
		
		assertThat(html)
				.contains("<iframe")
				.contains("sandbox=\"" + EdusharingHtmlSanitizer.IFRAME_SANDBOX + "\"")
				.doesNotContain("h5p-resizer.js");
	}
	
	@Test
	public void shouldKeepPdfIframe() throws Exception {
		String html = sut.sanitize(load("render_es10_pdf.html"));
		
		assertThat(html).contains("<iframe").contains("pdfJS");
	}
	
	@Test
	public void shouldKeepVideo() throws Exception {
		String html = sut.sanitize(load("render_es10_video.html"));
		
		assertThat(html).contains("<video").contains(".mp4").doesNotContain("<script")
				.contains("<button").contains("data-url=\"https://edu-sharing.example.org:443/esrender/modules/cache/video/");
	}
	
	@Test
	public void shouldKeepIframeOfAllowedHost() throws Exception {
		String html = sut.sanitize(load("render_es10_youtube.html"));
		
		assertThat(html).contains("<iframe").contains("www.youtube-nocookie.com/embed/").contains("sandbox=");
	}
	
	@Test
	public void shouldRemoveIframeOfOtherHost() {
		String html = sut.sanitize("<iframe src=\"https://evil.example.org/x\"></iframe><iframe src=\"//player.vimeo.com/video/1\"></iframe>");
		
		assertThat(html).doesNotContain("<iframe");
	}
	
	@Test
	public void shouldRemoveMediaOfOtherHost() {
		String html = sut.sanitize("<img src=\"https://evil.example.org/x.png\"><video src=\"https://evil.example.org/x.mp4\"></video>");
		
		assertThat(html).doesNotContain("evil.example.org");
		assertThat(sut.sanitize("<li data-url=\"https://evil.example.org/x.mp4\">240p</li>")).doesNotContain("evil.example.org");
	}
	
	@Test
	public void shouldRemoveJavascriptUrl() {
		String html = sut.sanitize("<a href=\"javascript:alert(1)\">x</a><iframe src=\"javascript:alert(1)\"></iframe>");
		
		assertThat(html).doesNotContain("javascript");
	}
	
	@Test
	public void shouldKeepTextOfUnknownTags() {
		String html = sut.sanitize("<a href=\"https://edu-sharing.example.org/x\"><es:title xmlns:es=\"x\">My title</es:title></a>");
		
		assertThat(html).contains("My title");
	}
	
	@Test
	public void shouldHandleEmpty() {
		assertThat(sut.sanitize(null)).isEmpty();
		assertThat(sut.sanitize(" ")).isEmpty();
	}
	
	private String load(String name) throws Exception {
		try (InputStream in = EdusharingHtmlSanitizerTest.class.getResourceAsStream(name)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
	
}
