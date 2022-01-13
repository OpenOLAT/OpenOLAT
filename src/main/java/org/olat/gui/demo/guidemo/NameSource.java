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
package org.olat.gui.demo.guidemo;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection.AutoCompletionSource;
import org.olat.core.gui.components.util.SelectionValues;

/**
 * 
 * Initial date: 6 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NameSource implements AutoCompletionSource {
	
	private static final List<String> ALL = List.of("Gloria Newton", "Herman Keith", "Rajah Edwards",
			"Christian Cummings", "Ulla Stewart", "Bevis Odom", "Emi Mullins", "Kellie Juarez", "Deirdre White",
			"Guinevere Duke", "India Brennan", "Ruby Hancock", "Libby Bray", "Ivor Mccormick", "Martin Calhoun",
			"Aaron Suarez", "Jaime Anthony", "Nigel Simon", "Dustin Knowles", "Thane Gordon", "Pamela Reid",
			"Martina Fowler", "Jescie Ballard", "Stacy Mathis", "Henry Hill", "Kenyon Doyle", "Octavia Oliver",
			"Nita Ashley", "Zelda Vance", "Curran Torres", "Cheryl Yates", "Lysandra Emerson", "Linda Hernandez",
			"Magee Stuart", "Elmo Wheeler", "Meredith Young", "Noelani Beck", "Irma Orr", "Myles Andrews", "Timon Kirk",
			"Lisandra Knox", "Maisie Mack", "Grant Sullivan", "Herrod Savage", "Charissa Fulton", "Myra Ryan",
			"Isabella Roy", "Allistair Fulton", "Daphne Merrill", "Brennan Arnold", "Shelly Newman", "Deirdre Thomas",
			"Mira Welch", "Dai Fuller", "Aphrodite Burns", "Rajah Little", "Felix Deleon", "Emma Bowen",
			"Bethany Zamora", "Ezra Carter", "Imogene Jennings", "Lacota Glass", "Yael Velasquez", "Sasha Madden",
			"Amanda Chavez", "Olympia Copeland", "Todd Stokes", "Althea Campbell", "Chase Bass", "Hiram Franklin",
			"Magee Chang", "Mannix Knox", "Griffin Rich", "Marny Spears", "Tate Mitchell", "Ivan Henson",
			"Urielle Bauer", "Pamela Gray", "Darrel Blanchard", "Cathleen Burks", "Ali Wilcox", "Raphael Mccray",
			"Marshall Miles", "Abel Nash", "Xantha Simpson", "Cassidy Bruce", "Allen Pruitt", "Demetria Bridges",
			"Fredericka Holt", "Acton Potts", "Maile Macdonald", "Roary Gardner", "Colt Dominguez", "Upton Snow",
			"Piper Nash", "Gary Sullivan", "Yen Hale", "Lucas Stephens", "Molly Carter", "Kathleen Whitaker",
			"Abdul Sparks", "Savannah Lambert", "Maxwell Patrick", "Macy Robles", "Deirdre Salas", "Deanna Miles",
			"Yasir Herring", "Blaze Wise", "Patience Sandoval", "Barry Whitehead", "Aidan Riley", "Wendy Vinson",
			"Gwendolyn Collins", "Jacqueline Lyons", "Alyssa Fowler", "Benedict Le", "Howard Sharpe", "Maggy Stewart",
			"Samson Petersen", "Yvonne Wall", "Simon Mcmahon", "Adrienne Hanson", "Germane Horne", "Ulric Branch",
			"Minerva Hines", "Whilemina Cox", "Vincent Slater", "Cedric Nguyen", "Zephania Mccullough", "Sybil Herrera",
			"Raymond Coleman", "Griffin Zamora", "Hamilton Carson", "Jaquelyn Pruitt", "Rose Hartman",
			"Whilemina Mcgee", "Christopher Warren", "Wylie Richard", "Melvin Barrett", "Rogan Fuller", "Colorado Roy",
			"Jennifer Mcgee", "Kennan Dominguez", "Chiquita Price", "Bell Webb", "Nasim Shelton", "Risa Bates",
			"Rhonda Sosa", "Steel Norman", "Ian Pearson", "Britanney Stanley", "Clinton Fulton", "Charde Hendricks",
			"Wendy Bartlett", "Otto Whitfield", "Kaye Morris", "Price Hendrix", "Malik Hunter", "Christian Noel",
			"Margaret Dickson", "Tanek Reeves", "Regan Moses", "Garrison Wolfe", "Davis Diaz", "Megan Dennis",
			"Penelope Pena", "Herrod Norton", "Doris Burris", "Avye Graham", "Kirby Beasley", "Wallace Johnson",
			"Alexander Marshall", "Sarah Nunez", "Jasmine Osborn", "Jakeem Snider", "Alexa Potter", "Herrod Dejesus",
			"Zeus Cantrell", "Daphne Dalton", "Zeph Washington", "Richard Merrill", "Demetria Gould", "Mona Crosby",
			"Deanna Erickson", "Aretha Fuentes", "Piper Rojas", "Gretchen Mcclure", "Angelica Campbell",
			"Vivian Galloway", "Stewart Haynes", "Brianna Mccullough", "Quon Gilmore", "Vance Keller", "Kai Cleveland",
			"Jarrod Macdonald", "Harding English", "Curran Macias", "Nadine Collier", "Sonia Reilly",
			"Cedric Frederick", "Shaine Matthews", "Rosalyn Wilkerson", "Margaret Mcfadden", "Yoshio Todd",
			"Armando Vinson", "Quin Herman", "Evangeline Conley", "Wang Cochran", "Hilary Horne", "Winifred Beach",
			"Jacob Nelson", "Zephania Alvarez", "Kylan Howard", "Alyssa Coffey", "Claire Curry", "Kenyon Salinas",
			"Lee Blanchard", "Clarke Ayala", "Dustin Mcpherson", "Abra Robles", "Griffith Weber", "Boris Berger",
			"Wang Steele", "Cruz Cabrera", "Mannix Marks", "Courtney Carrillo", "Idola Glass", "Beatrice Glass",
			"Yeo Townsend", "Darius Huber", "Quinn Morgan", "Tatyana Mccullough", "Carson Boyer", "Rafael Pittman",
			"Hayley Simon", "Reuben O'donnell", "Tucker Salas", "Hadley Curtis", "Ulysses Mercado", "Cheyenne Clay",
			"Elmo Byers", "Vincent Rocha", "Benedict Knapp", "Carla Velez", "Yolanda Delacruz", "Lavinia Underwood",
			"Breanna Ellis", "Malcolm Williams", "Talon Erickson", "Graham Poole");
	
	@Override
	public SelectionValues getSelectionValues(Collection<String> keys) {
		SelectionValues selectionValues = new SelectionValues();
		ALL.stream().filter(value -> keys.contains(value))
				.forEach(value -> selectionValues.add(entry(value, value)));
		return selectionValues;
	}
	
	@Override
	public SearchResult getSearchResult(String searchText) {
		List<String> filtered = ALL.stream().filter(value -> value.toLowerCase().contains(searchText)).sorted().collect(Collectors.toList());
		
		int countTotal = filtered.size();
		List<String> result = filtered.size() > 15? filtered.subList(0, 14): filtered;
		int countCurrent = result.size();
		SelectionValues selectionValues = new SelectionValues();
		result.forEach(key -> selectionValues.add(entry(key, key)));
		
		return new SearchResult(countTotal, countCurrent, selectionValues);
	}
	
}
