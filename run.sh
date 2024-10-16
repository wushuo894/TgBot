#!/bin/bash

jar="TgBot-jar-with-dependencies.jar"

stop() {
  pid=$(ps -ef | grep java | grep "$jar" | awk '{print $2}')
  if [ -n "$pid" ]; then
      echo "Stopping process $pid - $jar"
      kill "$pid"
  fi
}

stop

nohup java -jar $jar &