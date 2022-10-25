#/bin/sh

CODEGEN_JAR=~/wutsi-codegen/wutsi-codegen.jar

API_NAME=security-manager
API_URL=https://raw.githubusercontent.com/wutsi/wutsi-openapi/master/src/openapi/v2/security.yaml
GITHUB_USER=WutsiTeam

echo "Generating code from ${API_URL}"
java -jar ${CODEGEN_JAR} server \
    -in ${API_URL} \
    -out . \
    -name ${API_NAME} \
    -package com.wutsi.security \
    -jdk 11 \
    -github_user ${GITHUB_USER} \
    -github_project ${API_NAME}-server \
    -heroku ${API_NAME}-server \
    -service_aws_mysql \
    -service_cache \
    -service_logger \
    -service_messaging \
    -service_mqueue \
    -service_slack

if [ $? -eq 0 ]
then
    echo Code Cleanup...
    mvn antrun:run@ktlint-format
    mvn antrun:run@ktlint-format

else
    echo "FAILED"
    exit -1
fi
