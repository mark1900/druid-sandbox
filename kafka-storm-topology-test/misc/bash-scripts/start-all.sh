#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}

cd "${DIR}/zookeeper-3.4.6"
./bin/zkServer.sh start
sleep 5

cd "${DIR}/kafka_2.10-0.8.2.1"
./bin/kafka-server-start.sh config/server.properties &
sleep 5

cd "${DIR}"
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-topology-test_v1_phase1
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-topology-test_v1_phase2


cd "${DIR}/apache-storm-0.9.4"
./bin/storm nimbus &
sleep 2
./bin/storm supervisor &
sleep 2
./bin/storm ui &
sleep 2

cd "${DIR}"