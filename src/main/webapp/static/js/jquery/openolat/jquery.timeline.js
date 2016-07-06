(function ($) {
   $.fn.timeline = function(options) {
    	var settings = $.extend({
    		parentContainerId:'',
            values: []
        }, options );
    	
    	try {
    		//this selected div
    		timelineItems(this, settings);
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
	};
  
	timelineItems = function($obj, settings) {
		var parentContainer = jQuery('#' + settings.parentContainerId);
    	var placeholderwidth = parentContainer.width();
		$obj.width(placeholderwidth);
		var placeholderheight = $obj.height();
		if(parentContainer.height() < 500) {
			placeholderheight = 500;
		}
		$obj.height(placeholderheight);

    	var values = settings.values;
    	var data = settings.values;
    	
    	var minTime = d3.min(values, function(d) { return d.time; });
    	var maxTime = d3.max(values, function(d) { return d.time; });

    	var margin = {top: 10, right: 60, bottom: 40, left: 120},
  	  		width = placeholderwidth - margin.left - margin.right,
  	  		height = placeholderheight - margin.top - margin.bottom;
		
    	var formatDate = d3.time.format('%d.%m');
    	var formatDates = function(d) { return formatDate(new Date(d)); };
    	
    	var x = d3.scale.linear()
	  		.domain([0, 1])
	  		.range([0, width]);
	
    	var xAxis = d3.svg.axis()
	  		.scale(x)
	  		.ticks(0)
	  		.orient('bottom');
		
		var y = d3.scale.linear()
  	  		.domain([minTime, maxTime])
  	  		.range([height, 0]);
		
		var yAxis = d3.svg.axis()
  	  		.scale(y)
  	  		.orient('left')
  	  		.tickFormat(formatDates)
  	  		.tickSize(-width, -width, -width);

		var svg = d3.select('#' + $obj.attr('id')).append('svg')
  	  		.attr('width', width + margin.left + margin.right)
  	  		.attr('height', height + margin.top + margin.bottom)
  	  		.append('g')
  	  		.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
		
		// x-axis
		svg.append("g")
		   .attr("class", "x axis")
		   .attr("transform", "translate(0," + height + ")")
		   .call(xAxis);

		// y-axis
		svg.append("g")
		   .attr("class", "y axis")
		   .call(yAxis)
		  .append("text")
		   .attr("class", "label")
		   .attr("transform", "rotate(-90)")
		   .attr("y", 6)
		   .attr("dy", ".71em")
		   .attr("dx", "-.71em")
		   .style("text-anchor", "end")
		   .text("Date");

		var maxCurveY = 10;
		var lineData = [];
		for(var i=0; i<(maxCurveY*20); i++) {
			lineData.push({y: (i == 0 ? 0 : i / 20.0)});
		}
		var lineX = d3.scale.linear().domain([0,1]).range([0, width]);
		var lineY = d3.scale.linear().domain([0,maxCurveY]).range([height, 0]);
		var curveY = d3.scale.linear().domain([minTime, maxTime]).range([0,maxCurveY]);
		  
		var lineGenerator = d3.svg.line()
		     .x(function(d) { return lineX(curvedX(d.y)); })
		     .y(function(d) { return lineY(d.y); })
		     .interpolate("linear");
		  
		var linePath = svg.append("path")
		   .data([lineData])
		   .attr("d", lineGenerator)
		   .attr("class", "o_timeline_curve");
		  
		// draw dots
		svg.selectAll(".dot")
		   .data(data)
		   .enter().append('g')
		   .append("circle")
		   .attr("class", function(d) { return "dot o_pf_status_" + d.status; })
		   .attr("r", 10)
		   .attr("cx", function(d) { return lineX(curvedX(curveY(d.time))); })
		   .attr("cy", function(d) { return y(d.time); })
		   .on('click', function(d) {
		    	//alert(d.status);  
		   });
	}
	
	curvedX = function(y) {
		return (1.5 + Math.sin(Math.sqrt(y))) / 3;
	}
}( jQuery ));