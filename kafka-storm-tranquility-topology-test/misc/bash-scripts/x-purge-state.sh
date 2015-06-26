#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}

BASE_DIR="${DIR}"
BACKUP_DIR="/tmp/backup-$(date +%Y%m%d_%H_%M_%S)"

mkdir -p ${BACKUP_DIR}/logs
cd /tmp
ls /tmp/ | grep -v "backup" | xargs -I {} mv {} ${BACKUP_DIR}/
cd ${DIR}


#find . -mindepth 2 -maxdepth 2 -path "*/log" -exec echo {} \;
#./druid-0.7.3/log
#find . -mindepth 2 -maxdepth 2 -path "*/logs" -exec echo {} \;
#./kafka_2.10-0.8.2.1/logs
#./apache-storm-0.9.4/logs


declare -a arr=("kafka_2.10-0.8.2.1/logs" "apache-storm-0.9.4/logs")

for i in "${arr[@]}"
do
   mkdir -p "${BACKUP_DIR}/${i}"
   mv -v ${BASE_DIR}/${i}/* ${BACKUP_DIR}/${i}/
done

echo "Purging individual log files."
find ${BASE_DIR} -name "*.log" -exec mv -v {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -path "*/log*/*.log*" -exec mv -v {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -name "*.out" -exec mv -v {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -path "*/log*/*.out*" -exec mv -v {} ${BACKUP_DIR}/logs/ \;



echo "***********************************"
echo ""
echo ""