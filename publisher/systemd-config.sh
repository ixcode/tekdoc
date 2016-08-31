[Unit]
Description=tekdoc - super awesome technical documentation
Documentation=http://ixcode.org/tekdoc

[Service]

Type=simple
User=tekdoc
ExecStart=/usr/bin/java -cp /home/tekdoc/.tekdoc/tekdoc.jar publisher.ci <PATH TO YOUR SITE CONFIG>/site.yml 


[Install]
WantedBy=multi-user.target 
