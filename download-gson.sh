#!/bin/bash
echo "Downloading Gson Library"
echo "-------------------------"

GSON_VERSION=2.10.1
DOWNLOAD_URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/${GSON_VERSION}/gson-${GSON_VERSION}.jar"
OUTPUT_FILE="lib/gson-${GSON_VERSION}.jar"

echo "Downloading Gson ${GSON_VERSION} from Maven Central..."
curl -L "${DOWNLOAD_URL}" -o "${OUTPUT_FILE}"

if [ $? -ne 0 ]; then
    echo "Failed to download Gson library!"
    exit 1
fi

echo ""
echo "Gson library downloaded successfully to ${OUTPUT_FILE}"
echo ""

# Make the script executable
chmod +x download-gson.sh
