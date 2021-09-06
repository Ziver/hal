#!/bin/bash

# -----------------------------------------------
# User input
# -----------------------------------------------

function printHelp() {
        echo "Wrapper for simplifying execution of Hal Server.
Usage:

    $(basename $0)
        [-f|--foreground]
        [-h|--help]

Where:
    --foreground        Run Hal Server in the foreground instead of detaching a new screen.
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

        -h|--help)
            printHelp
            exit 0
        ;;

        *)
            echo "ERROR: Unknown input parameter: $1=$2"
            echo ""
            printHelp
            exit 1
        ;;
    esac

    shift
done

# -----------------------------------------------
# Execute
# -----------------------------------------------

# gradle returns normally when doing ctr-c so we need to add a
# trap where the bash script exits instead of continuing.
trap 'exit 130' INT

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


