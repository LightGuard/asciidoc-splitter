#!/usr/bin/env bash

unset -v SOURCE_REPO
unset -v SOURCE_BRANCH
unset -v OUTPUT_REPO
unset -v OUTPUT_BRANCH
unset -v IGNORE
unset -v ATTRIBS

PARSED_ARGS=$(getopt -a -n asciidoc-splitter -o -i:vVha: -l sr:,sb:,or:,ob: -- "$@")
VALID_ARGS=$?

JAR_DIR=$(dirname "$0")/../lib
JAR_NAME=${project.artifactId}-${project.version}.jar
SPLITTER_COMMAND_BASE="java -cp ${JAR_DIR}/${JAR_NAME}:${JAR_DIR}/* com.redhat.documentation.asciidoc.cli.ExtractionRunner"
HELP_COMMAND="${SPLITTER_COMMAND_BASE} -h"

if [[ "$VALID_ARGS" -ne "0" ]]; then
  echo "Could not parse arguments."
  eval ${HELP_COMMAND}
  exit 1;
fi

eval set -- "${PARSED_ARGS}"
while :
do
  case "$1" in
    --sr) SOURCE_REPO="$2"   ; shift 2  ;;
    --sb) SOURCE_BRANCH="$2" ; shift 2  ;;
    --or) OUTPUT_REPO="$2"   ; shift 2  ;;
    --ob) OUTPUT_BRANCH="$2" ; shift 2  ;;
      -i) IGNORE="-i $2"        ; shift 2  ;;
      -a) ATTRIBS="-a $2"       ; shift 2  ;;
      -h) eval ${HELP_COMMAND} ; break ;;
    --) shift; break ;;
    *) echo "Unknown option: $1"
      exit 2 ;;
  esac
done

# If there is no git info, die
if [[ -z ${SOURCE_REPO+x} ]]; then
    echo "No source repo given, cannot continue."
    eval ${HELP_COMMAND}
    exit 3
fi

if [[ -z ${SOURCE_BRANCH+x} ]]; then
    echo "No source branch given, cannot continue."
    eval ${HELP_COMMAND}
    exit 4
fi

if [[ -z ${OUTPUT_REPO+x} ]]; then
    echo "No output repo given, cannot continue."
    eval ${HELP_COMMAND}
    exit 5
fi

if [[ -z ${OUTPUT_BRANCH+x} ]]; then
    echo "No output branch given, cannot continue."
    eval ${HELP_COMMAND}
    exit 6
fi

# Check if the branch we want to check out exists
git ls-remote --exit-code --heads ${SOURCE_REPO} ${SOURCE_BRANCH} &> /dev/null
SOURCE_BRANCH_EXISTS=$?

if [[ "${SOURCE_BRANCH_EXISTS}" -ne "0" ]]; then
    echo "Source branch does not exist in the remote repo, cannot continue."
    exit 7
fi

# Check if the branch we want to commit to exists
git ls-remote --exit-code --heads  ${OUTPUT_REPO} ${OUTPUT_BRANCH} &> /dev/null
OUTPUT_BRANCH_EXISTS=$?

if [[ "${OUTPUT_BRANCH_EXISTS}" -ne "0" ]]; then
    echo "Output branch does not exist in the remote repo, cannot continue."
    exit 8
fi

TEMP_CHECKOUT_DIR="$(mktemp -d)"
TEMP_WORK_DIR="$(mktemp -d)"

echo "TEMP_CHECKOUT_DIR: ${TEMP_CHECKOUT_DIR}"
echo "TEMP_WORK_DIR: ${TEMP_WORK_DIR}"

echo "Cloning origin..."

git clone --depth 1 -b ${SOURCE_BRANCH} ${SOURCE_REPO} ${TEMP_CHECKOUT_DIR} &> /dev/null
git clone -b ${OUTPUT_BRANCH} ${OUTPUT_REPO} ${TEMP_WORK_DIR} &> /dev/null
eval "${SPLITTER_COMMAND_BASE} -s ${TEMP_CHECKOUT_DIR} -o ${TEMP_WORK_DIR} ${IGNORE} ${ATTRIBS}"

JAVA_EXIT=$?

if [[ "${JAVA_EXIT}" -ne "0" ]]; then
    exit 9
fi

#echo "Committing work and pushing..."
#cd ${TEMP_WORK_DIR}
#git add . &> /dev/null
#git commit -m 'Running asciidoc-splitter' -a &> /dev/null
#git push -f -u origin ${OUTPUT_BRANCH} &> /dev/null

# Clean up
#rm -rf ${TEMP_CHECKOUT_DIR}
#rm -rf ${TEMP_WORK_DIR}
