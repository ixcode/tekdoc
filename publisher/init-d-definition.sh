#!/bin/sh
RUN_AS=tekdoc
SERVICE_NAME=tekdoc-ci
PATH_TO_JAR=/usr/${RUN_AS}/.tekdoc/tekdoc.jar
PID_PATH_NAME=/tmp/${SERVICE_NAME}
SITE_CONFIG=/home/${RUN_AS}/git-checkout/technical-documentation/publisher/tekdoc/site.yml
LOG_DIR=/var/log/tekdoc
LOG_OUT=${LOG_DIR}/tekdoc.log
LOG_ERR=${LOG_DIR}/tekdoc-err.log
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -d ${LOG_DIR} ]; then
	mkdir ${LOG_DIR}
        fi
        
        if [ ! -f $PID_PATH_NAME ]; then
	/sbin/runuser -u ${RUN_AS} -s /bin/bash -c "nohup java -cp /home/${RUN_AS}/.tekdoc/tekdoc.jar publisher.ci ${SITE_CONFIG} >> ${LOG} 2>> ${LOG_ERR} < /dev/null & ; echo $! > $PID_PATH_NAME"            
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
