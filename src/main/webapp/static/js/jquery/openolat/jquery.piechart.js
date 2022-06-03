(function ( $ ) {
	"use strict";
	$.fn.ooPieChart = function(options) {
		var settings = $.extend({
			w: 600,	// Width of the circle
			h: 600,	// Height of the circle
			margin: 20,
			layer: 0,
			colors: [],
			entries: [],
			title: null,
			subTitle: null
		}, options);

		try {
			pieChart(this, settings);
		} catch(e) {
			if(window.console) console.log(e);
		}
		
		function pieChart($obj, cfg) {
			// set the dimensions and margins of the graph
			var width = cfg.w;
			var height = cfg.h;
			var margin = cfg.margin;
			var layer = cfg.layer;
			var dataEntries = settings.entries;
			var colorRange = settings.colors;
			var title = settings.title;
			var subTitle = settings.subTitle;
			
			// The radius of the pieplot is half the width or half the height (smallest one). I subtract a bit of margin.
			var radius = Math.min(width, height) / 2 - margin
			var innerRadius = 0;
			if(layer > 0) {
				innerRadius = Math.max(10, radius - layer);
			}
			
			// append the svg object to the div called 'my_dataviz'
			var id = $obj.attr('id');
			var svg = d3.select("#" + id)
			  .append("svg")
			    .attr("width", width)
			    .attr("height", height)
			  .append("g")
			    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
			
			// Compute the position of each group on the pie:
			var pie = d3.pie()
			  .startAngle(0)
			  .sort(function (a, b) {
          			return a.key.localeCompare(b.key);
          	   })
			  .value(function(d) {return d.value; });
			
			var dataReady = pie(dataEntries);
			
			// Build the pie chart: Basically, each part of the pie is a path that we build using the arc function.
			var chart = svg
			  .selectAll('whatever')
			  .data(dataReady)
			  .enter()
			  .append('path')
			  .attr('d', d3.arc()
			    .innerRadius(innerRadius)         // This is the size of the donut hole
			    .outerRadius(radius)
			  )
			  .attr("class", function(d) { return d.data.cssClass; })
			  .style("stroke-width", "0px");
			
			if(title != null) { 
				svg
				  .append("text")
				  .attr("y", "-5")
				  .attr("text-anchor", "middle")
				  .attr("class", "o_piechart_title")
				  .text(title);
			}
			if(subTitle != null) { 
				svg
				  .append("text")
				  .attr("y", "20")
				  .attr("text-anchor", "middle")
				  .attr("class", "o_piechart_subtitle")
				  .text(subTitle);
			}
		}
	}
}( jQuery ));