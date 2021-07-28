#!/bin/bash

#ant clean
./gradlew build

# Kill current session
screen -S hal -X kill
# Start new session
screen -S hal -L -d -m ./gradlew run

echo "-----------------"
screen -list
