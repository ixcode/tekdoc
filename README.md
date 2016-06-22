# TEKDOC - the architectural documentation facility

This project is an attempt to make a configurable (in terms of style) but consistent generator for web sites that describe architectures.

It's mostly a static site generator based around markdown, but it has some core concepts within it that are specific to architecture documentation so its not so generic.

# TODO

- Provide a way to deep link to a location in another md file - e.g. glossaries - need an anchor
- Provide a glossary concept so you don't have to build the link manually for every word you want to glossarise.
- Allow _layouts to live outside the content dir? OR move static files into the content dir. Might mean things render more naturally?
- Make it possible to refer to a layout in extends by aboslute path?
