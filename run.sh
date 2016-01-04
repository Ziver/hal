#!/bin/bash
#screen -S hal -L -d -m \
#	 java -cp sqlite-jdbc-3.7.2.jar:jSerialComm-1.3.4.jar:hal.jar:. se.koc.hal.plugin.tellstick.TelstickSerialCommTest

#ant clean
ant build
screen -S hal -L -d -m ant run-main-server

echo "-----------------"
screen -list

