#!/bin/bash

ant clean
ant release
screen -S hal -L -d -m ant run

echo "-----------------"
screen -list

