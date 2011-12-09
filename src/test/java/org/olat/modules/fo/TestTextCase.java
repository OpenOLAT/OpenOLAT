/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo;

public class TestTextCase {

	private static Text[] cases = {
		new Text("fr", 11, 47, "Et voici un petit message en fran\u00e7ais. J'en ai plus qu'assez."),
		new Text("fr", 8, 31, "Je veux parler et je veux \u00eatre entendu."),
		new Text("de", 36, 203, "Im Forum gibt es f\u00fcr Moderatoren die M\u00f6glichkeit eine \u00dcbersicht \u00fcber die Beitr\u00e4ge der im Forum vertretenen Personen anzuzeigen. Auf dieser Liste sollen auch die Anzahl der geschriebenen Zeichen und wenn m\u00f6glich auch W\u00f6rter angezeigt werden."),
		new Text("ja", 18, 72, "\u30ea\u30f4\u30ea\u30fc\u30ac\u30fc\u30c7\u30f3 \u7279\u5178 \u300cPC\u7248\u30ea\u30f4\u30ea\u30fc\u30a2\u30a4\u30e9\u30f3\u30c9\u3067\u4f7f\u7528\u3067\u304d\u308b\u30c8\u30e9\u30f3\u30b7\u30ed\u30f3\u30ab\u30fc\u30c9(3\u679a\u30bb\u30c3\u30c8)\u300d&Amazon.co.jp\u30aa\u30ea\u30b8\u30ca\u30eb\u300c\u30e9\u30f4\u30ea\u30fc\u30ab\u30ec\u30f3\u30c0\u30fc(\u5353\u4e0a\u30b5\u30a4\u30ba)\u300d\u4ed8"),	
		new Text("ja", 16, 52, "\u6cbb\u627f\u4e09\u5e74\u5341\u4e00\u6708\u5341\u4e94\u65e5\u3001\u5165\u9053\u5949\u6068\u671d\u5bb6\u7531\u805e\u3048\u3057\u304b\u5171\u3001\u9759\u61b2\u6cd5\u5370\u9662\u5ba3\u306e\u5fa1\u4f7f\u306b\u3066\u3001\u69d8\u3005\u4f1a\u91c8\u7533\u3051\u308c\u3070\u3001\u4e8b\u306e\u5916\u306b\u304f\u3064\u308d\u304e\u7d66\u305f\u308a"),
		new Text("en", 36, 149, "With so many iPhone's/ Touches and the apps that use ads like it was made just to sell you stuff, this data seems reasonable. While I understand the purpose of ads, I dislike them very much."),
		new Text("zh", 12, 26, "\u5353\u8d8a\u4e9a\u9a6c\u900a\u5411\u60a8\u4fdd\u8bc1\u6240\u552e\u5546\u54c1\u4e3a\u6b63\u54c1\u884c\u8d27\uff0c\u5e76\u53ef\u63d0\u4f9b\u6b63\u89c4\u53d1\u7968"),
	};
	
	public static Text[] getCases() {
		return cases;
	}
	
	public static class Text {
		private String text;
		private int words;
		private int characters;
		private String language;
		
		public Text(String language, int words, int characters, String text) {
			this.text = text;
			this.words = words;
			this.characters = characters;
			this.language = language;
		}

		public int getWords() {
			return words;
		}

		public void setWords(int words) {
			this.words = words;
		}

		public int getCharacters() {
			return characters;
		}

		public void setCharacters(int characters) {
			this.characters = characters;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}
	}
}
