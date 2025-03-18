#! /bin/bash

JAVA_START="java -jar app.jar"
FINAL_JAVA_START=''
FINAL_JAVA_START_ECHO=''
ENV_MAPPING_FILE="/home/dummy/config/env_mapping.csv"


validateEnv(){
  local varName=$1
  if [ -z "${!varName}" ]; then
    echo "Error: ${varName} is not set. Please set it in the run command or compose file."
    exit 1
  fi
}

appendArgument(){
  local argName=$1
  local varName=$2
  local value="${!varName}"

  FINAL_JAVA_START+=" ${argName}=\"${value}\""
  FINAL_JAVA_START_ECHO+=" \\ \n${argName}=\"${value}\""
}

waitForDb(){
  echo "Waiting for database to be ready."
  sleep 5
}

startSpringApp(){
  echo "All checks completed."
  echo -e "Starting spring app with the following properties:
${JAVA_START}${FINAL_JAVA_START_ECHO}
"
  eval "${JAVA_START}${FINAL_JAVA_START}"
}

function run(){
  echo "Validating required environment variables."
  while IFS=',' read -r varName argName; do
    if [[ "$varName" != "EnvironmentVariable" ]]; then
      validateEnv "${varName}"
      appendArgument "$argName" "$varName"
    fi
  done < "$ENV_MAPPING_FILE"
  echo "Environment variables have passed validation."

  waitForDb
  startSpringApp
}

run
