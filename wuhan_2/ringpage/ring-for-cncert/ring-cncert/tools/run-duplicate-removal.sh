#!/bin/bash

# run-duplicate-removal [update|compile|start|stop|restart|status]

CMD=$1
cd /var/app/ring-for-cncert/ring-cncert


NLPIR_DIR="`pwd`/data/src/main/resources/nlpir"
SVM_DIR="`pwd`/data/src/main/resources"

export PROPERTIES="-Dnlpir.dir=$NLPIR_DIR -Dsvm.dir=$SVM_DIR"

PID_FILE=/tmp/duplicate-removal.pid
PID=`cat $PID_FILE`

OUT=/tmp/duplicate-removal.out
ERR=/tmp/duplicate-removal.err

COUNT="`ps $PID | grep $PID | grep -v 'grep' | wc -l`"

export MAVEN_OPTS="-Xmx4096m -XX:MaxPermSize=8192M $PROPERTIES"

function start(){
    if [ $COUNT -gt 0 ]; then
        echo "duplicate-removal task is already running";
    else
        cd task;
        mvn exec:java -Dexec.mainClass="act.ring.cncert.task.nlp.DuplicateRemoval" >$OUT 2>$ERR &
        echo $! >$PID_FILE;
        echo "starting duplicate-removal";
    fi
}

function stop(){
    if [ $COUNT -gt 0 ]; then
        echo "stopping duplicate-removal";
        kill -9 $PID;
    else
        echo "duplicate-removal is not running";
    fi
}

function status(){
    if [ $COUNT -gt 0 ]; then
        echo "duplicate-removal is running";
    else
        echo "duplicate-removal is not running";
    fi
}

case $CMD in
    "update")
        git pull;;
    "compile")
        mvn compile;;
    "start")
        start;;
    "stop")
        stop;;
    "restart")
        stop;
        sleep 1s;
        start;;
    "status")
        status;;
    "out")
        echo $OUT | xargs $2;;
    "err")
        echo $ERR | xargs $2;;
* ) echo "only accept update,compile,start,stop,restart,status,out,err" >&2;;
esac
