# TEKDOC - the architectural documentation facility

This project is an attempt to make a configurable (in terms of style) but consistent generator for web sites that describe architectures.

It's mostly a static site generator based around markdown, but it has some core concepts within it that are specific to architecture documentation so its not so generic.

There are a couple of sub directories - your probably most interested in the `publisher` directory which is what you want to install (see below).

## Installation

You will need leiningen installed. If you have homebrew (http://brew.sh/) installed then its easy..

	brew install leiningen

Then clone this repo somewhere and type:

	cd publisher
	lein uberjar

This will create an executable java binary (jarfile) something like `target/publisher-0.1.0-SNAPSHOT-standalone.jar`

You can then run a script `tekdoc.sh` which will copy this jarfile and script into your home directory from where you can use it.

	./tekdoc.sh install

Now you should be able to publish your site!

# TODO

- Provide a way to deep link to a location in another md file - e.g. glossaries - need an anchor
- Provide a glossary concept so you don't have to build the link manually for every word you want to glossarise.
- Allow _layouts to live outside the content dir? OR move static files into the content dir. Might mean things render more naturally?
- Make it possible to refer to a layout in extends by aboslute path?

- Make it so that when you publish you don't destroy the python server because youve deleted the output root

- Implement a Preview serving live from the source to improve iteration time

- Implement "publish spreadsheet" - specify a range in a sheet in a spreadsheet to extract and publish as a table. That way you can use the spreadsheet to maintain the underlying data.
