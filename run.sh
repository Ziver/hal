#!/bin/bash

#ant clean
ant package-all

# Kill current session
screen -S hal -X kill
# Start new session
screen -S hal -L -d -m ./gradlew run

echo "-----------------"
screen -list

