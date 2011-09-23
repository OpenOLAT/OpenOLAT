package org.olat.commons.coordinate.cluster.jms;

public class SimpleProbe {

	private long total_;
	private int num_;
	private long max_;
	
	public synchronized void addMeasurement(long value) {
		total_ += value;
		num_++;
		if (value>max_) {
			max_ = value;
		}
	}
	
	public int getAvg() {
		return Math.round((float)total_/(float)num_);
	}
	
	public long getMax() {
		return max_;
	}
	
	public int getNum() {
		return num_; 
	}
	
	public synchronized void reset() {
		total_ = 0;
		num_ = 0;
		max_ = 0;
	}
	
}
