#!/bin/bash

ant clean
# Kill current session
screen -S hal -X kill
# Start new session
screen -S hal -L -d -m ant run

echo "-----------------"
screen -list

