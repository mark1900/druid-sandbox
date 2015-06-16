#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${DIR}

BASE_DIR="${DIR}"
BACKUP_DIR="/tmp/backup-$(date +%Y%m%d_%H_%M_%S)"

mkdir -p ${BACKUP_DIR}/logs
cd /tmp
ls /tmp/ | grep -v "backup" | xargs -I {} mv {} ${BACKUP_DIR}/
cd ${DIR}

find ${BASE_DIR} -name "*.log" -exec mv {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -path "*/log*/*.log*" -exec mv {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -name "*.out" -exec mv {} ${BACKUP_DIR}/logs/ \;
find ${BASE_DIR} -path "*/log*/*.out*" -exec mv {} ${BACKUP_DIR}/logs/ \;

#mkdir -p ${BACKUP_DIR}/apache-storm-0.9.4
#mv ${BASE_DIR}/apache-storm-0.9.4/tmp ${BACKUP_DIR}/apache-storm-0.9.4/

echo "***********************************"
echo ""
echo ""
echo "REMEMBER TO PURGE ANY PERSISTANT STORES such as the PostgreSQL DATABASE."
echo ""
