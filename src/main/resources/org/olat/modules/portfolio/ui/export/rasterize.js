var page = require('webpage').create(),
    system = require('system'),
    address, output;

if (system.args.length != 3) {
    console.log('Usage: rasterize.js URL filename');
    phantom.exit(1);
} else {
    address = system.args[1];
    output = system.args[2];
    page.viewportSize = { width: 600, height: 600 };
    page.paperSize = { format: 'A4', orientation: 'portrait', margin: '1cm' };

    page.open(address, function (status) {
        if (status !== 'success') {
            console.log('Unable to load the address!');
            phantom.exit(1);
        } else {
        	// Finally render as PDF
            window.setTimeout(function () {
                page.render(output);
                phantom.exit();
            }, 200);
        }
    });
}