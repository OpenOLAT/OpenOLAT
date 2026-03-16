/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 5 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationStatisticElement {
	
	private Integer sum;
	private Integer min;
	private Integer max;
	private List<Integer> reviews = new ArrayList<>();
	
	private final ReviewElementDefinition elementDefinition;

	public ApplicationStatisticElement(ReviewElementDefinition elementDefinition) {
		this.elementDefinition = elementDefinition;
	}
	
	public ReviewElementDefinition getElementDefinition() {
		return elementDefinition;
	}
	
	public String getLabel() {
		return elementDefinition.getLabel();
	}
	
	public String getType() {
		return elementDefinition.getType() == null ? "" : elementDefinition.getType().name();
	}
	
	public Integer getSum() {
		return sum;
	}
	
	public Integer getMin() {
		return min;
	}
	
	public Integer getMax() {
		return max;
	}
	
	public Double getAverage() {
		if(reviews.isEmpty() || sum == null) {
			return null;
		}

		double average = sum.doubleValue() / reviews.size();
		return Double.valueOf(average);
	}
	
    public double getVariance() {
        double mean = sum.doubleValue() / reviews.size();

        double temp = 0;
        for(Integer review :reviews) {
        	double a = review.doubleValue();
            temp += (mean-a)*(mean-a);
        }
        return temp / reviews.size();
    }

    /**
     * Population standard deviation
     * 
     * @return A double or null 
     */
    public Double getStandardDeviation() {
		if(reviews.isEmpty() || sum == null) {
			return null;
		}
    	
        double stdDev =  Math.sqrt(getVariance());
		return Double.valueOf(stdDev);
    }
	
	public int getNumOfReviews() {
		return reviews.size();
	}
	
	public void addValue(Integer val) {
		if(val == null) return;
		
		if(sum == null) {
			sum = val;
		} else {
			sum = Integer.sum(sum.intValue(), val.intValue());
		}
		
		if(min == null || min.intValue() > val.intValue()) {
			min = val;
		}
		if(max == null || max.intValue() < val.intValue()) {
			max = val;
		}
		reviews.add(val);
	}
	
	
}
