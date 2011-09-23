// * * * * * * * * * * * * * * * * * * *
// java script plasma for web application easter eggs
// (c) 20.01.2005 by florian@gnaegi.ch
// license: 
// you can use and modify this script as long as you don't sell it and include 
// this lines allong with the script.
// i programmed this because i always wanted to programm a plasma. then i was 
// looking for an easter eg that i could implement into the open source learning 
// management system olat (http://www.olat.org), so  i decided to finally code
// a small plasma together with a scrolling text using java script and css... 
// usage:
// to start the plasma, press shift, alt or control key while double clicking 
// somewhere on the page. have fun and send me nice emails or pay pal money.
// * * * * * * * * * * * * * * * * * * *

// configuration
// number of pixels in x and y axis
var x = 20;
var y = 20;
// pixel scaling factor
var f = 10;
// space between pixels
var s = 0;
// number of iterations
var q = 500;
// flow text at right side: plain text - no html allowed!
var t = "thank you for using the \nlearning management\nsystem olat\n\n" 
	+ "olat is beeing developed by:\nroberto bagnoli\nhanspeter brun\npatrick brunner\nlavinia dumitrescu\n"
	+ "lars eberle\nstefan egli\nclaudia ehrle\njoel fisler\nflorian gnaegi\nchristian guretzki\nroman haag\n"
	+ "stefan hofstetter\nkristina isacson\ndesiree iturbide\nsabina jeger\nfelix jost\nandreas kapp\nmartin kernland\ningmar kroll\n"
	+ "tiziana perini\nmaurus rohrer\nsandra roth\nalexander schneider\nfranziska schneider\nguido schnider\nmaya schuessler\nruth schuermann\n"
	+ "renata sevcikova\nconi steinemann\nmike stock\nalexandra stucki\ngregor wassmann\nmarion weber\ncarsten weisse\ntimo wuersch\nhans-joerg zuberbuehler\n"
	+ "\n\nopen source development \nfinanced by\nUniversity of Zuerich\n"
	+ "\n\n(c) 1999-2009 by\nwww.olat.org"
	+ "\n\n\n\n\n\n\n\n\n\n\n\n\njava script plasma \n(c) 2009 by\ngnaegi@frentix.com\n\n\n\n\n\n\n\n\n\n\n\n\n";
// flow text speed factor
var ts = 1.5;
// flow text font size
var fs = "8pt";

// * * * * * do not change below * * * * * 
var p;		// pixels width and height
var c;		// iteration counter
var px;		// current iteration x coordinate
var py;		// current iteration y coordinate
var plasma;	// window interval name 
var pixels;	// pixels cache
var w;		// plasma and text area width 
var h;		// plasma and text area height
var blocked;	// iteration monitor

function plasmaInit() {
	// initialize runtime parameters
	p = parseInt(f) + "px"; // pixels width and height
	c = 0;  // iteration counter
	px = 0; // current iteration x coordinate
	py = 0; // current iteration y coordinate
	plasma; // window interval name 
	pixels = new Array(); // pixels cache
	w = (x*f + s*x - s);  // plasma and text area width 
	h = (y*f + s*y - s);  // plasma and text area height
	blocked = false;
}

function plasmaIter() {
	// iterate over the plasma and modify each pixels color accordingly
	// aquire monitor
	if (blocked) return;
	blocked = true;
	// for all rows in this iteration update pixel color
	while(py < y) {
		// calcualte current pixes coordinates
		var xpx = parseInt(px*f + px*s) + "px";
		var ypx = parseInt(py*f + py*s) + "px";
		var id = xpx + "|" + ypx;
		// get the pixel from the pixel cache
		var pixel = pixels[id]; 
		// calculate the rgb values for this pixel. now here is the magic
		var r = parseInt(Math.ceil(Math.abs( (126 * Math.sin(((px+c*2) / x * 90 ) / 57)) + (126 * Math.sin(((py+c) / y * 90 ) / 57)) )));
		var g = parseInt(Math.ceil(Math.abs( (126 * Math.sin(((px-c) / x * 90 ) / 57)) + (126 * Math.sin(((py-c*2) / y * 90 ) / 57)) )));
		var b = parseInt(Math.ceil(Math.abs( (255 * Math.sin((c / q * 90) / 57)))));		
		var color = "rgb(" + r + "," + g + "," + b + ")";
		// update div now
		pixel.style.background = color;
		// increment x and y counter
		if (px < x-1) px++;
		else {
			// reset x coutner
			px = 0;
			if (py < y) py++;
		}
	}
	// let the text flow up a bit
	var plasmaFlowtext = document.getElementById("plasmaFlowtext");
	var textposition = parseInt(h - c*ts) + "px";
	plasmaFlowtext.style.top = textposition; 
	// reset y counter and increment the iteration counter
	py = 0; 
	c++;
	// release monitor
	blocked = false;
	// stop everithing if iteration end reached
	if (c >= q) plasmastop(null);
}

function plasmastop(event) {
	// stops the plasma and removes everything
	// shift, alt or ctr key must be pressed to stop
	if (event != null && !(event.shiftKey || event.ctrlKey || event.altKey)) return;
	// 1) stops the plasma iterations
	window.clearInterval(plasma);
	// 2) get plasma container and remove it
	var plasmaContainer = document.getElementById("plasmaContainer");
	// 2.1) remove flow text if not already removed. this is the case when the user
	// clicked on the flowtext
	var plasmaFlowtext = document.getElementById("plasmaFlowtext");
	if (plasmaFlowtext != null) plasmaContainer.removeChild(plasmaFlowtext);
	// 2.2) remove plasma pixels when not already removed. this is the case when 
	// the user clicken on a pixel
	for (i=0; i < y; i++) {
		for (j=0; j < x; j++) {
			var ypx = parseInt(i*f + i*s) + "px";
	 		var xpx = parseInt(j*f + j*s) + "px";
			var id = xpx + "|" + ypx;
			var pixel = document.getElementById(id);
			if (pixel != null) plasmaContainer.removeChild(pixel);
		}
	}
	// 2.3) remove container
	var body = document.getElementsByTagName("body")[0];
	body.removeChild(plasmaContainer);
	// 3) listen to double click events to start plasma again
	window.ondblclick = jsplasma;	
}

function jsplasma(event) {
	// paints the plasma and initiates the plasma iterations
	// shift, alt or ctr key must be pressed to start
	if (!(event.shiftKey || event.ctrlKey || event.altKey)) return;
	// 1) init plasma global variables
	plasmaInit();
	// 2) create plasmaContainer div
	var plasmaContainer = document.createElement("div");
	plasmaContainer.id = "plasmaContainer";
	plasmaContainer.style.background = "white";
	plasmaContainer.style.overflow = "hidden";
	plasmaContainer.style.border = "1px solid black";
	plasmaContainer.style.position = "absolute";
	plasmaContainer.style.top = "40%";
	plasmaContainer.style.left = "40%";
	plasmaContainer.style.width = parseInt(w*2) + "px";
	plasmaContainer.style.height = parseInt(h)+ "px";
	plasmaContainer.style.zIndex=10000;
	// 3) add text flow div to plasmaContainer
	var plasmaFlowtext = document.createElement("div");
	plasmaFlowtext.id = "plasmaFlowtext";
	plasmaFlowtext.style.position = "relative";
	plasmaFlowtext.style.top = parseInt(h) +"px";
	plasmaFlowtext.style.left = parseInt(w) + "px";
	plasmaFlowtext.style.width = parseInt(w) + "px";
	plasmaFlowtext.style.textAlign="center";
	plasmaFlowtext.style.fontSize = fs;
	plasmaFlowtext.style.fontFamily = "verdana, sans-serif";
	plasmaFlowtext.style.whiteSpace = "pre";
   	var text = document.createTextNode(t);
   	plasmaFlowtext.appendChild(text);
   	plasmaContainer.appendChild(plasmaFlowtext);
	// 4) add plasma pixels to plasmaContainer
	for (i=0; i < y; i++) {
		for (j=0; j < x; j++) {
			var ypx = parseInt(i*f + i*s) + "px";
	 		var xpx = parseInt(j*f + j*s) + "px";
			var id = xpx + "|" + ypx;
			var pixel = document.createElement("div");
	 		pixel.id = id;
			pixel.style.background = "#eee";
			pixel.style.position = "absolute";
			pixel.style.top = ypx;
			pixel.style.left = xpx;
			pixel.style.width = p;
			pixel.style.height = p;
			// add to plasmaContainer div
			plasmaContainer.appendChild(pixel);
			// add to pixel cache
			pixels[id]=pixel;
		}
	}
 	// 5) put plasmaContainer as direct child object of body tag to have correct positioning
 	var myBody = document.getElementsByTagName("body")[0];
 	myBody.insertBefore(plasmaContainer, myBody.firstChild);
	// 6) start plasma iterations
	plasma = window.setInterval("plasmaIter()",10);
	// 7) listen to double click events to stop plasma
	window.ondblclick = plasmastop;
}

// start plasma when getting double click event (plus one of shift, alt or ctr keys)
window.ondblclick = jsplasma;