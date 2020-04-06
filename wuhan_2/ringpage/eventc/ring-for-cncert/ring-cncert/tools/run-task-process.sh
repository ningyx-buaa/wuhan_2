#!/bin/bash

# run-task-process [update|compile|start|stop|restart|status]

CMD=$1
cd /var/app/ring-for-cncert/ring-cncert

NLPIR_DIR="`pwd`/data/src/main/resources/nlpir"
SVM_DIR="`pwd`/data/src/main/resources"

export PROPERTIES="-Dnlpir.dir=$NLPIR_DIR -Dsvm.dir=$SVM_DIR"

PID_FILE=/tmp/task-process.pid
PID=`cat $PID_FILE`

OUT=/tmp/task-process.out
ERR=/tmp/task-process.err

COUNT="`ps -ef | grep $PID | grep -v 'grep' | wc -l`"

export MAVEN_OPTS="-Xmx8192m -XX:MaxPermSize=4096M $PROPERTIES"

function start(){
    if [  $COUNT -gt 0 ];then
        echo "task-process is already runing, stop it first";
    else
        cd task;
        mvn exec:java -Dexec.mainClass="act.ring.cncert.task.process.Driver" >$OUT 2>$ERR &
        echo $! >$PID_FILE;
        echo "starting task-process";
    fi
}

function stop(){
    if [  $COUNT -gt 0 ];then
        echo "stopping task-process";
        kill -9 $PID;
    else
        echo "task-process not started";
    fi
}

function status(){
    if [  $COUNT -gt 0 ];then
        echo "task-process is runing";
    else
        echo "task-process is stopped";
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
