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

MODE="FOREGROUND"

until [[ $# -eq 0 ]]; do
    case "$1" in
        -f|--foreground)
            MODE="FOREGROUND"
            shift
        ;;

        -b|--background)
            MODE="BACKGROUND"
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
# Functions
# -----------------------------------------------

function startHal {
    local ARGS=$1
    local CLASSPATH="$(cd "${INSTALL_DIR}" && find . -name "*.jar" -type f -printf '%p:')"
    CLASSPATH=${CLASSPATH::-1}

    (set -x && cd "${INSTALL_DIR}" && java -classpath "${CLASSPATH}" se.hal.HalServer ${ARGS})
    local EXIT_CODE=$?

    return ${EXIT_CODE}
}

function createSymLink {
    local file=$1

    if [[ -f "./${file}" ]]; then
        echo "INFO: Creating symlink for: ${file}"
        ln -s -f "$(realpath "./${file}")" "${INSTALL_DIR}/${file}"
    fi
}

# -----------------------------------------------
# Execute
# -----------------------------------------------
# Prepare execution

# gradle returns normally when doing ctr-c so we need to add a
# trap where the bash script exits instead of continuing.
trap 'exit 130' INT

INSTALL_DIR="."

if [[ -d "./hal-core/src" ]]; then
    # We are operating on source code so build Hal
    echo "INFO: Operating in source directory, will build Hal."
    INSTALL_DIR="./build/install/Hal"

    ./gradlew --console=rich installDist
    EXIT_CODE=$?

    if [[ ${EXIT_CODE} -ne 0 ]]; then
        exit 1
    fi

    echo ""
    createSymLink "hal.conf"
    createSymLink "hal.db"
fi

# Execute

echo ""
echo "INFO: Starting Hal in the ${MODE}."

if [[ ${MODE} == "FOREGROUND" ]]; then
    EXIT_CODE=200

    while [[ ${EXIT_CODE} -eq 200 ]]; do
        # Restart as long as we have a exit code of 200, this allows the application to restart itself
        startHal
        EXIT_CODE=$?
    done
elif [[ ${MODE} == "BACKGROUND" ]]; then
    # Kill current session
    screen -S hal -X kill
    # Start new session
    screen -S hal -L -d -m ./run --foreground

    echo "-------------------------"
    screen -list
else
   echo "ERROR: Unknown mode: ${MODE}"
   exit 1
fi
