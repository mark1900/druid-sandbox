#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}

cd "${DIR}/zookeeper-3.4.6"
./bin/zkServer.sh start
sleep 1

cd "${DIR}/kafka_2.10-0.8.2.1"
./bin/kafka-server-start.sh config/server.properties &
sleep 1
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-tranquility-druid-topology-test_v1


cd "${DIR}/apache-storm-0.9.4"
./bin/storm nimbus &
sleep 2
./bin/storm supervisor &
sleep 2
./bin/storm ui &
sleep 2

cd "${DIR}/druid-0.7.3"
java -Xmx2g -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/overlord:lib/*:${HADOOP_CONFIG_PATH} io.druid.cli.Main server overlord &
sleep 5
java -Xms64m -Xmx64m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/middlemanager:lib/*:${HADOOP_CONFIG_PATH} io.druid.cli.Main server middleManager &
sleep 2
java -Xmx256m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/coordinator:lib/* io.druid.cli.Main server coordinator &
sleep 2
java -Xmx256m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/broker:lib/* io.druid.cli.Main server broker &
sleep 2

cd "${DIR}"