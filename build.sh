#!/bin/bash

echo "Movie Streaming App Build Script"
echo "------------------------------"

if [ ! -d "bin" ]; then
    mkdir bin
fi

echo ""
echo "Compiling Java files..."
javac -cp "lib/*" -d bin src/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Compilation successful!"
echo ""

echo "Choose an option:"
echo "1. Run the main application"
echo "2. Initialize database with test data"
echo ""

read -p "Enter your choice (1 or 2): " choice

if [ "$choice" = "1" ]; then
    echo ""
    echo "Running the main application..."
    echo ""
    java -cp "bin:lib/*" MovieStreamingApp
elif [ "$choice" = "2" ]; then
    echo ""
    echo "Initializing database with test data..."
    echo ""
    java -cp "bin:lib/*" DatabaseTest
else
    echo ""
    echo "Invalid choice!"
fi
