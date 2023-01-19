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
package org.olat.core.util.filter.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.olat.core.util.CodeHelper;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamEventProcessor;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamEventReceiverWrapper;
import org.owasp.html.PolicyFactory;

import com.google.common.base.Predicate;

/**
 * The policy allow very specific onclicks values. It has a pre and
 * post processor to handle the javascript:parent.goto(273846)
 * 
 * 
 * Initial date: 22 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenOLATPolicy {
	
	private static final String MEDIA_HOST = "http://my" + CodeHelper.getForeverUniqueID() + "localhost:8123/";

	private static final Pattern PARAGRAPH = Pattern.compile("([\\p{L}\\p{N},'\\.\\s\\-_\\(\\)]|&[0-9]{2};)*");
	private static final Pattern COLORNAME = Pattern.compile("(aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|rebeccapurple|purple|red|silver|teal|white|yellow)");
	private static final Pattern OFFSITEURL = Pattern.compile("(\\s)*((ht)tp(s?)://)[\\p{L}\\p{N}]+[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*(\\s)*");
	private static final Pattern SPECIALPROTOCOLS = Pattern.compile("(\\s)*(mailto:|tel:)[\\+\\p{L}\\p{N}]+[\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*(\\s)*");	
	private static final Pattern HTMLCLASS = Pattern.compile("[a-zA-Z0-9\\s,-_]+");
	private static final Pattern ANYTHING = Pattern.compile(".*");
	private static final Pattern ONSITEURL = Pattern.compile("([\\p{L}\\p{N}\\p{Zs}/\\.\\?=&\\-~_]|ccrep:)+");
	private static final Pattern ANCHOR = Pattern.compile("#[a-zA-Z0-9_]*");
	private static final Pattern NUMBER = Pattern.compile("[0-9]+");
	private static final Pattern HTMLTITLE = Pattern.compile("[a-zA-Z0-9\\s-_',:\\[\\]!\\./\\\\\\(\\)%&;\\+#]*");
	private static final Pattern OLATINTERNALURL = Pattern.compile("javascript:parent\\.goto(node|tool)\\(['\"]?[a-zA-Z0-9]+['\"]?\\)");
	private static final Pattern NUMBERORPERCENT = Pattern.compile("(\\d)+(%{0,1})");
	private static final Pattern COLORCODE = Pattern.compile("(#([0-9a-fA-F]{6}|[0-9a-fA-F]{3}))");
	private static final String[] VARIABLES = new String[] { "$courseUrl", "$groupUrl", "$curriculumUrl" };

	public static final PolicyFactory POLICY_DEFINITION = new HtmlPolicyBuilder()
		.allowStyling()
		.allowAttributes("id")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:]+")).globally()
		.allowAttributes("title")
			.matching(HTMLTITLE).globally()
		.allowAttributes("lang")
			.matching(Pattern.compile("[a-zA-Z]{2,20}")).globally()
			
		.allowUrlProtocols("http", "https")
			.allowElements("img", "a", "video", "audio")
			.allowUrlProtocols("mailto", "tel")
			.allowElements("a")
		// Fix::dir
		.allowAttributes("charoff")
			.matching(Pattern.compile("numberOrPercent"))
			.onElements("tbody")
		.allowAttributes("char")
			.matching(Pattern.compile(".*{0,1}"))
			.onElements("tbody")
		.allowAttributes("valign")
			.matching(false,"baseline", "bottom", "middle", "top")
			.onElements("tbody")
		.allowAttributes("align")
			.matching(false,"center", "middle", "left", "right", "justify", "char")
			.onElements("tbody")
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("div", "ul", "blockquote", "figcaption")
		// img
		.allowAttributes("border")
			.matching(NUMBER).onElements("img")
		.allowAttributes("src")
			.matching(new Patterns(ONSITEURL, OFFSITEURL)).onElements("img")
		.allowAttributes("alt")
			.matching(PARAGRAPH).onElements("img")
		.allowAttributes("align")
			.matching(false,"center", "middle", "left", "right", "justify", "char").onElements("img")
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("img")
		.allowAttributes("hspace")
			.matching(NUMBER).onElements("img")
		.allowAttributes("height")
			.matching(NUMBERORPERCENT).onElements("img")
		.allowAttributes("vspace")
			.matching(NUMBER).onElements("img")
		.allowAttributes("width")
			.matching(NUMBERORPERCENT).onElements("img")
		// video, audio
		.allowAttributes("src")
			.matching(new Patterns(ONSITEURL, OFFSITEURL)).onElements("audio", "video")	
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("audio", "video")
		.allowAttributes("height")
			.matching(NUMBER).onElements("video")
		.allowAttributes("width")
			.matching(NUMBER).onElements("video")
		// edu-sharing
		.allowAttributes("data-es_identifier")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:]+")).onElements("img")
		.allowAttributes("data-es_width")
			.matching(NUMBER).onElements("img")
		.allowAttributes("data-es_mediatype")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:]+")).onElements("img")
		.allowAttributes("data-es_objecturl")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\/]+")).onElements("img")
		.allowAttributes("data-es_show_infos")
			.matching(Pattern.compile("show|hide")).onElements("img")
		.allowAttributes("data-es_mimetype")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\/]+")).onElements("img")
		.allowAttributes("data-es_height")
			.matching(NUMBER).onElements("img")
		.allowAttributes("data-es_version_current")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\.]+")).onElements("img")
		.allowAttributes("data-es_first_edit")
			.matching(Pattern.compile("true|false")).onElements("img")
		.allowAttributes("data-es_version")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\.]+")).onElements("img")
		.allowAttributes("data-es_show_license")
			.matching(Pattern.compile("show|hide")).onElements("img")

		.allowAttributes("charoff").matching(Pattern.compile("numberOrPercent")).onElements("thead")
		.allowAttributes("char").matching(Pattern.compile(".*{0,1}")).onElements("thead")
		.allowAttributes("valign").matching(false,"baseline", "bottom", "middle", "top").onElements("thead")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("thead")
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("h1", "h2", "h3", "h4", "h5", "h6")	
		.allowAttributes("class").matching(HTMLCLASS).onElements("ol")
		.allowAttributes("border").matching(NUMBER).onElements("table")
		.allowAttributes("summary").matching(PARAGRAPH).onElements("table")
		.allowAttributes("bgcolor").matching(new Patterns(COLORNAME, COLORCODE)).onElements("table")
		.allowAttributes("background").matching(ONSITEURL).onElements("table")
		.allowAttributes("cellpadding").matching(NUMBER).onElements("table")
		.allowAttributes("noresize").matching(false,"noresize").onElements("table")
		.allowAttributes("width").matching(NUMBERORPERCENT).onElements("table")
		.allowAttributes("cellspacing").matching(NUMBER).onElements("table")
		.allowAttributes("rules").matching(false,"none", "rows", "cols", "groups", "all").onElements("table")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("table")
		.allowAttributes("class").matching(HTMLCLASS).onElements("table")
		.allowAttributes("height").matching(NUMBERORPERCENT).onElements("table")
		// link
		.allowAttributes("alt")
			.matching(PARAGRAPH).onElements("a")
		.allowAttributes("nohref")
			.matching(false,"nohref").onElements("a")
		.allowAttributes("target")
			.matching(false,"_blank").onElements("a")
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("a")
		.allowAttributes("rel")
			.matching(false,"nofollow").onElements("a")
		.allowAttributes("href")
			.matching(new OrPredicate(new Patterns(ONSITEURL, OFFSITEURL, OLATINTERNALURL, ANCHOR, SPECIALPROTOCOLS), new StringsPredicate(VARIABLES)))
			.onElements("a")
	    .allowAttributes("onclick")
			.matching(new OnClickValues())
			.onElements("a")
		// link edu-sharing
		.allowAttributes("data-es_show_infos")
			.matching(Pattern.compile("show|hide")).onElements("a")
		.allowAttributes("data-es_identifier")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:]+")).onElements("a")
		.allowAttributes("data-es_width")
			.matching(NUMBER).onElements("a")
		.allowAttributes("data-es_mediatype")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:]+")).onElements("a")
		.allowAttributes("data-es_objecturl")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\/\\.]+")).onElements("a")
		.allowAttributes("data-es_mimetype")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\/]+")).onElements("a")
	    .allowAttributes("data-es_height")
	    	.matching(NUMBER).onElements("a")
		.allowAttributes("data-es_version_current")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\.]+")).onElements("a")
		.allowAttributes("data-es_first_edit")
			.matching(Pattern.compile("true|false")).onElements("a")
		.allowAttributes("data-es_version")
			.matching(Pattern.compile("[a-zA-Z0-9_\\-\\:\\.]+")).onElements("a") 
		.allowAttributes("data-es_show_license")
			.matching(Pattern.compile("show|hide")).onElements("a")
		// figure
		.allowAttributes("class")
			.matching(HTMLCLASS)
			.onElements("figure")
		.allowAttributes("class")
			.matching(HTMLCLASS)
			.onElements("i")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("p")
		.allowAttributes("charoff").matching(Pattern.compile("numberOrPercent")).onElements("tfoot")
		.allowAttributes("char").matching(Pattern.compile(".*{0,1}")).onElements("tfoot")
		.allowAttributes("valign").matching(false,"baseline", "bottom", "middle", "top").onElements("tfoot")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("tfoot")
		.allowAttributes("headers").matching(Pattern.compile("[a-zA-Z0-9\\s*]*")).onElements("td")
		.allowAttributes("nowrap").matching(ANYTHING).onElements("td")
		.allowAttributes("valign").matching(false,"baseline", "bottom", "middle", "top").onElements("td")
		.allowAttributes("axis").matching(Pattern.compile("[a-zA-Z0-9\\s*,]*")).onElements("td")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("td")
		.allowAttributes("colspan").matching(NUMBER).onElements("td")
		.allowAttributes("bgcolor").matching(new Patterns(COLORNAME, COLORCODE)).onElements("td")
		.allowAttributes("charoff").matching(Pattern.compile("numberOrPercent")).onElements("td")
		.allowAttributes("background").matching(ONSITEURL).onElements("td")
		.allowAttributes("scope").matching(false,"row", "col", "rowgroup", "colgroup").onElements("td")
		.allowAttributes("rowspan").matching(NUMBER).onElements("td")
		.allowAttributes("width").matching(NUMBERORPERCENT).onElements("td")
		.allowAttributes("char").matching(Pattern.compile(".*{0,1}")).onElements("td")
		.allowAttributes("abbrev").matching(PARAGRAPH).onElements("td")
		.allowAttributes("height").matching(NUMBERORPERCENT).onElements("td")
		.allowAttributes("headers").matching(Pattern.compile("[a-zA-Z0-9\\s*]*")).onElements("th")
		.allowAttributes("nowrap").matching(ANYTHING).onElements("th")
		.allowAttributes("valign").matching(false,"baseline", "bottom", "middle", "top").onElements("th")
		.allowAttributes("axis").matching(Pattern.compile("[a-zA-Z0-9\\s*,]*")).onElements("th")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("th")
		.allowAttributes("colspan").matching(NUMBER).onElements("th")
		.allowAttributes("bgcolor").matching(new Patterns(COLORNAME, COLORCODE)).onElements("th")
		.allowAttributes("charoff").matching(Pattern.compile("numberOrPercent")).onElements("th")
		.allowAttributes("scope").matching(false,"row", "col", "rowgroup", "colgroup").onElements("th")
		.allowAttributes("rowspan").matching(NUMBER).onElements("th")
		.allowAttributes("width").matching(NUMBERORPERCENT).onElements("th")
		.allowAttributes("char").matching(Pattern.compile(".*{0,1}")).onElements("th")
		.allowAttributes("abbrev").matching(PARAGRAPH).onElements("th")
		.allowAttributes("height").matching(NUMBERORPERCENT).onElements("th")
		.allowAttributes("charoff").matching(Pattern.compile("numberOrPercent")).onElements("tr")
		.allowAttributes("background").matching(ONSITEURL).onElements("tr")
		.allowAttributes("width").matching(NUMBERORPERCENT).onElements("tr")
		.allowAttributes("char").matching(Pattern.compile(".*{0,1}")).onElements("tr")
		.allowAttributes("valign").matching(false,"baseline", "bottom", "middle", "top").onElements("tr")
		.allowAttributes("align").matching(false,"center", "middle", "left", "right", "justify", "char").onElements("tr")
		.allowAttributes("class").matching(HTMLCLASS).onElements("tr")
		.allowAttributes("height").matching(NUMBERORPERCENT).onElements("tr")
		.allowAttributes("class")
			.matching(HTMLCLASS).onElements("span")
		
		.allowElements("dd","tbody","dl","caption","hr","div","dt","ul","init","blockquote","pre","em","figcaption","sub",
				"strong","img","thead","h1","h2","h3","h4","h5","h6","sup","ol","table","b","figure","strike","i","p",
				"tfoot","td","s","th","u","li","tr", "span", "video", "audio")
		
		.allowElements("hr")
			.allowWithoutAttributes("hr")
		.allowElements("br")
			.allowWithoutAttributes("br")
		.allowElements("a")
			.allowWithoutAttributes("a")
		.allowElements("img")
			.allowWithoutAttributes("img")
		.allowElements("object")
			.allowWithoutAttributes("object")
		.allowElements("applet")
			.allowWithoutAttributes("applet")
		.allowElements("param")
			.allowWithoutAttributes("param")
		.allowElements("meta")
			.allowWithoutAttributes("meta")
		.allowElements("embed")
			.allowWithoutAttributes("embed")
		.allowElements("basefont")
			.allowWithoutAttributes("basefont")
		.allowElements("col")
			.allowWithoutAttributes("col")
		.allowElements("span")
			.allowWithoutAttributes("span")
		.allowElements("center")
			.allowWithoutAttributes("center")
		.withPreprocessor(new OpenOLATPreprocessor())
		.withPostprocessor(new OpenOLATPostprocessor())
		.toFactory();

	private static class OpenOLATPreprocessor implements HtmlStreamEventProcessor {

		@Override
		public HtmlStreamEventReceiver wrap(HtmlStreamEventReceiver sink) {
			return new OpenOLATPreReceiver(sink);
		}
	}
	
	private static class OpenOLATPreReceiver extends HtmlStreamEventReceiverWrapper {
		
		public OpenOLATPreReceiver(HtmlStreamEventReceiver sink) {
			super(sink);
		}

		@Override
		public void openTag(String elementName, List<String> attrs) {
			if("a".equals(elementName) && attrs != null) {
				int numOfAttrs = attrs.size();
				for(int i=0; i<numOfAttrs; i++) {
					String attr = attrs.get(i);
					if("href".equals(attr) && i+1 < numOfAttrs
							&& attrs.get(i+1).startsWith("javascript:parent.goto")
							&& OLATINTERNALURL.matcher(attrs.get(i + 1)).matches()) {
						attrs.set(i, "onclick");
					} else if("href".equals(attr) && i+1 < numOfAttrs
							&& attrs.get(i+1).startsWith("media/")) {
						attrs.set(i + 1, MEDIA_HOST + attrs.get(i+1));
					}
				}
			}
			super.openTag(elementName, attrs);
		}
	}
	
	private static class OpenOLATPostprocessor implements HtmlStreamEventProcessor {

		@Override
		public HtmlStreamEventReceiver wrap(HtmlStreamEventReceiver sink) {
			return new OpenOLATPostReceiver(sink);
		}
	}
	
	private static class OpenOLATPostReceiver extends HtmlStreamEventReceiverWrapper {
		
		public OpenOLATPostReceiver(HtmlStreamEventReceiver sink) {
			super(sink);
		}

		@Override
		public void openTag(String elementName, List<String> attrs) {
			if("a".equals(elementName) && attrs != null) {
				int numOfAttrs = attrs.size();
				for(int i=0; i<numOfAttrs; i++) {
					String attr = attrs.get(i);
					if("onclick".equals(attr) && i+1 < numOfAttrs
							&& attrs.get(i+1).startsWith("javascript:parent.goto")
							&& OLATINTERNALURL.matcher(attrs.get(i + 1)).matches()) {
						attrs.set(i, "href");
					} else if("href".equals(attr) && i+1 < numOfAttrs
							&& attrs.get(i+1).startsWith(MEDIA_HOST)) {
						attrs.set(i + 1, attrs.get(i+1).substring(MEDIA_HOST.length()));
					}	
				}
			}
			super.openTag(elementName, attrs);
		}
	}
	
	private static class OnClickValues implements Predicate<String> {
		
		@Override
		public boolean apply(String s) {
			if("o_XHRWikiEvent(this);".equals(s) || "o_XHRWikiEvent(this);return(false);".equals(s)) {
				return true;
			}
			return OLATINTERNALURL.matcher(s).matches();
		}
		
		// Needed for Java8 compat with later Guava that extends
		// java.util.function.Predicate.
		// For some reason the default test method implementation that calls
		// through to apply is not assumed here.
		@Override
		public boolean test(String s) {
			return apply(s);
		}
	}
	
	private static class OrPredicate implements Predicate<String> {
		
		private final Predicate<String> a;
		private final Predicate<String> b;
		
		public OrPredicate(Predicate<String> a,  Predicate<String> b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public boolean apply(String s) {
			return a.apply(s) || b.apply(s);
		}
		
		@Override
		public boolean test(String s) {
			return apply(s);
		}
	}
	
	private static class StringsPredicate implements Predicate<String> {

		private final String[] strings;
		
		public StringsPredicate(String[] strings) {
			this.strings = strings;
		}
		
		@Override
		public boolean apply(String s) {
			for(int i=strings.length; i-->0; ) {
				if(strings[i].equalsIgnoreCase(s)) {
					return true;
				}
			}
			return false;
		}
		
		// Needed for Java8 compat with later Guava that extends
		// java.util.function.Predicate.
		// For some reason the default test method implementation that calls
		// through to apply is not assumed here.
		@Override
		public boolean test(String s) {
			return apply(s);
		}
	}
	
	private static class Patterns implements Predicate<String> {
		
		private final Pattern a;
		private final Pattern b;
		private final Pattern c;
		private final Pattern d;
		private final Pattern e;
		
		public Patterns(Pattern a, Pattern b) {
			this(a, b, null, null, null);
		}
		
		public Patterns(Pattern a, Pattern b, Pattern c, Pattern d, Pattern e) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
		}

		@Override
		public boolean apply(String s) {
			return a.matcher(s).matches()
					|| b.matcher(s).matches()
					|| c == null || c.matcher(s).matches()
					|| d == null || d.matcher(s).matches()
					|| e == null || e.matcher(s).matches();
		}
		
		// Needed for Java8 compat with later Guava that extends
		// java.util.function.Predicate.
		// For some reason the default test method implementation that calls
		// through to apply is not assumed here.
		@Override
		public boolean test(String s) {
			return apply(s);
		}
	}
}
