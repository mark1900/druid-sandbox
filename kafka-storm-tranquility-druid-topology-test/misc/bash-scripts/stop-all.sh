#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}

ps -ef | grep java | grep -v "grep" | grep -v " org.apache.zookeeper.server.quorum.QuorumPeerMain " | grep -v " kafka.Kafka " | awk '{print $2}'| sort -r | xargs kill -SIGTERM
sleep 5

cd "${DIR}/kafka_2.10-0.8.2.1"
./bin/kafka-server-stop.sh config/server.properties &
sleep 5

cd "${DIR}/zookeeper-3.4.6"
./bin/zkServer.sh stop
sleep 1
