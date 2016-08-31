# TEKDOC publisher

This application publishes a set of templates and markdown files into a static website.

Yes it's another static site generator.

But its focused on publishing a technical documentation website. You know, like that wiki you have in your organisation where information goes to die.

## CI Installation

CI server can be configured with systemd.

- Create a user called tekdoc that has access to your web publishing root (assuming /var/www/html as its the nginx default).

You need to install the tekdoc.service file in `/usr/lib/systemd/system/tekdoc.service`

	sudo systemctl daemon-reload
	sudo systemctl start tekdoc.service
	sudo systemctl status tekdoc.service

You can follow the logs here:

	journalctl -fu tekdoc.service 
