#!/usr/bin/env bash
coco_jar_url=https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar
project_dir="$(dirname "$0")/../"
cd "$project_dir" || exit
mkdir -p ./lib
if [[ ! -f ./lib/Coco.jar ]]; then
    curl --insecure --output lib/Coco.jar $coco_jar_url
fi
java -jar lib/Coco.jar \
  -package org.matwoess.jsourceprofiler.tool.instrument \
  jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg
