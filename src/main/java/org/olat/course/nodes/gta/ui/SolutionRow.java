package org.olat.course.nodes.gta.ui;

import org.olat.course.nodes.gta.model.Solution;

/**
 * 
 * Initial date: 02.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SolutionRow {
	
	private final Solution solution;
	private final String author;
	
	public SolutionRow(Solution solution, String author) {
		this.solution = solution;
		this.author = author;
	}

	public Solution getSolution() {
		return solution;
	}

	public String getAuthor() {
		return author;
	}
}
