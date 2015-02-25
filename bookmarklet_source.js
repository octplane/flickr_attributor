var l=window.location.href;
l = "http://fa.oct.zoy.org/flickr/" + l.replace(/https:\/\/www.flickr.com\/photos\/[^\/]+\//,"").replace(/\/.*/,"");
window.open(l);

// minify using http://chriszarate.github.io/bookmarkleter/
// embed in (function() { ... })()