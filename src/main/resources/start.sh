#!/bin/sh -x
NLS_LANG=AMERICAN_AMERICA.ZHS16GBK
LANG=zh_CN.UTF-8
APP_DIR=/app/appsystems
SPRING_HOME=/app/springboot
AGENT_HOME=/app/skywalking-agent
LOG_DIR=/app/applogs
server_port=${SERVER_PORT}
service_name=${SERVICE_NAME}
ip_addr=`hostname -i`
hostname=${HOSTNAME}

profiles=${SPRING_PROFILES}


# define a dump_dir bind with start_date
START_DATE=$(date +%Y%m%d-%H%M%S)
DUMP_DIR=$LOG_DIR/dump



# start java
if [ -f "${JAVA_HOME}/bin/java" ]; then
   JAVA=${JAVA_HOME}/bin/java
else
   JAVA=java
fi
export JAVA

PATH=${PATH}:$JAVA_HOME/bin

JAVA_MEM_OPTS="${JVM_HEAP_SIZE:--Xms1024M -Xmx1024M -Xmn256M} -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -verbose:gc -Xloggc:$LOG_DIR/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$DUMP_DIR/start-${START_DATE}.hprof -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=30005 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false   "
JAVA_OPTIONS="${JAVA_MEM_OPTS} -DSW_AGENT_NAME=${service_name} -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=172.17.49.80:11800  -Dinstance.log.home=${LOG_DIR}  -DLOG_DIR=${LOG_DIR} -DSERVER_NAME=${hostname}  -Dfile.encoding=utf-8 -Dlogging.path=${LOG_DIR}"
CLASSPATH="$APP_DIR/config/:`ls $APP_DIR/*.jar 2>/dev/null|awk '{printf $1":"}'`"


if [ -z "$profiles" ]
then
    echo "profiles is empty"
    EXTRA_JAVA_OPTIONS="org.springframework.boot.loader.JarLauncher --server.port=${server_port}   "
else
    echo "profiles is not empty"
    EXTRA_JAVA_OPTIONS="org.springframework.boot.loader.JarLauncher --server.port=${server_port}  --spring.profiles.active=${profiles}  "
fi


export EXTRA_JAVA_OPTIONS
JAVA_OPTIONS=" ${JAVA_OPTIONS} -classpath ${CLASSPATH} $EXTRA_JAVA_OPTIONS"
export NLS_LANG LANG JAVA_HOME JAVA_OPTIONS CLASSPATH PATH  LOG_DIR
echo '$JAVA ${JAVA_OPTIONS}'
$JAVA ${JAVA_OPTIONS}
