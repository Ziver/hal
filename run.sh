#!/bin/bash

# -----------------------------------------------
# User input
# -----------------------------------------------

function printHelp() {
        echo "Run code analysis through SonarQube.
Usage:

    $(basename $0)
        [-f|--foreground]
	[-h|--help]

Where:
    --foreground        Run Hal Server in the foreground instead of detatching a new screen.
    --help              Print this help message.
    "
}

MODE="SCREEN"

until [[ $# -eq 0 ]]; do
    case "$1" in
        -f|--forground)
            MODE="FORGROUND"
            shift
        ;;

        *)
            echo "ERROR: Unknown input parameter: $1=$2"
            exit 1
        ;;
    esac

    shift
done

# -----------------------------------------------
# Execute
# -----------------------------------------------

# Build Hal source
./gradlew build

if [[ ${MODE} == "FORGROUND" ]]; then
    ./gradlew run
else 
    # Kill current session
    screen -S hal -X kill
    # Start new session
    screen -S hal -L -d -m ./gradlew run

    echo "-------------------------"
    screen -list
fi


