#!/bin/bash

# ~~~~~~~~~~~~~~~~~~~~~~~~~~
# ILP Submission Test Script
# ~~~~~~~~~~~~~~~~~~~~~~~~~~

# exit on error
set -e 

# colours for output 
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# function for animating spinning loader
spinner() {
    local pid=$1
    local message=$2
    local spinstr=('⠋' '⠙' '⠹' '⠸' '⠼' '⠴' '⠦' '⠧' '⠇' '⠏') 
    local i=0
    
    while kill -0 $pid 2>/dev/null; do
        printf "\r${YELLOW} [%s] %s${NC}" "${spinstr[$i]}" "$message"
        i=$(( (i + 1) % ${#spinstr[@]} ))
        sleep 0.1
    done
    printf "\r%*s\r" $(( ${#message} + 10 )) ""  # Clear entire line properly
}

# function to run command with spinner
run_with_spinner() {
    local message=$1
    shift
    local cmd="$@"
    
    # Run command in background and capture output
    eval "$cmd" > /tmp/spinner_output 2>&1 &
    local pid=$!
    
    spinner $pid "$message"
    
    wait $pid
    local exit_code=$?
    
    return $exit_code
}


# config
TAR_FILE="ilp_submission_image.tar"
STUDENT_ID="${1:-sXXXXXX}"
PORT=8080
CONTAINER_NAME="${STUDENT_ID}"

echo -e "══════════════════════════════"
echo -e "ILP Docker Submission Test    "
echo -e "══════════════════════════════\n"

# Step 1: Check/create tar file
run_with_spinner "Step 1: Checking if tar file exists..." "sleep 0.5"

if [ ! -f "${TAR_FILE}" ]; then
    printf "\r%*s\r" 50 ""
    echo -e "${YELLOW}⚠ Tar file not found${NC}"
    
    # Try to find and build from Dockerfile
    if [ -f "Dockerfile" ]; then
        echo -e "Found Dockerfile, building image..."
    
        # Remove old ilp_cw1 image if it exists
        if docker images ilp_cw1 | grep -q ilp_cw1; then
            run_with_spinner "Removing old ilp_cw1 image..." "docker rmi -f ilp_cw1 > /dev/null 2>&1"
            echo -e "${GREEN}✓ Old image removed${NC}"
            sleep 0.5
        fi
        
        set +e  # Disable exit on error
        run_with_spinner "Building Docker image..." "docker build -t ilp_cw1 . > /tmp/docker_build_output 2>&1"
        BUILD_EXIT_CODE=$?
        set -e  # Re-enable exit on error
        
        if [ $BUILD_EXIT_CODE -eq 0 ]; then
            echo -e "${GREEN}✓ Image built successfully${NC}"
            sleep 0.5
            
            # Save to tar
            run_with_spinner "Creating tar file..." "docker image save ilp_cw1 -o $TAR_FILE"
            echo -e "${GREEN}✓ Step 1: Tar file created${NC}"
            sleep 0.5
        else
            echo -e "${RED}✗ Build failed${NC}"
            cat /tmp/docker_build_output
            exit 1
        fi
    else
        echo -e "${RED}✗ No Dockerfile or tar file found${NC}"
        echo -e "${YELLOW}Make sure you're in your project directory with a Dockerfile${NC}"
        exit 1
    fi
else
    printf "\r%*s\r" 50 ""
    echo -e "${GREEN}✓ Step 1: Tar file found${NC}"
    sleep 0.5
fi

# Step 2: Cleanup with spinner
run_with_spinner "Step 2: Cleaning up existing containers..." "docker rm -f '$CONTAINER_NAME' 2>/dev/null || true; docker rmi -f ilp_test_image 2>/dev/null || true"
echo -e "${GREEN}✓ Step 2: Cleanup complete${NC}"
sleep 0.5

# Step 3: Load the Docker image
set +e
run_with_spinner "Step 3: Loading Docker image from tar file..." "docker image load -i '$TAR_FILE' > /tmp/docker_load_output 2>&1"
LOAD_EXIT_CODE=$?
set -e

if [ $LOAD_EXIT_CODE -eq 0 ]; then
    # Extract image ID or tag
    IMAGE_ID=$(grep -oP '(?<=Loaded image: |Loaded image ID: sha256:)\S+' /tmp/docker_load_output | head -1)
    
    if [ -z "$IMAGE_ID" ]; then
        echo -e "${RED}✗ Step 3: Failed to extract image ID${NC}"
        echo "Load output:"
        cat /tmp/docker_load_output
        exit 1
    fi
    
    # Tag the image for easier reference
    run_with_spinner "Tagging image as ilp_test_image..." "docker tag '$IMAGE_ID' ilp_test_image > /dev/null 2>&1"
    echo -e "${GREEN}✓ Step 3: Image loaded successfully${NC}"
    sleep 0.5
else
    echo -e "${RED}✗ Step 3: Failed to load image${NC}"
    echo "Error output:"
    cat /tmp/docker_load_output
    exit 1
fi

# Step 4: Start container
set +e
run_with_spinner "Step 4: Starting Docker container..." "docker run -d --publish $PORT:$PORT --name '$CONTAINER_NAME' ilp_test_image > /tmp/docker_run_output 2>&1"
RUN_EXIT_CODE=$?
set -e

if [ $RUN_EXIT_CODE -eq 0 ]; then
    CONTAINER_ID=$(docker ps -a --filter "name=$CONTAINER_NAME" --format "{{.ID}}" | head -1)
    
    if [ -z "$CONTAINER_ID" ]; then
        echo -e "${RED}✗ Step 4: Container created but ID not found${NC}"
        echo "Output:"
        cat /tmp/docker_run_output
        exit 1
    fi
    
    echo -e "${GREEN}✓ Step 4: Container created (ID: ${CONTAINER_ID:0:12})${NC}"
    sleep 0.5
else
    echo -e "${RED}✗ Step 4: Failed to start container${NC}"
    echo "Error output:"
    cat /tmp/docker_run_output
    exit 1
fi

# Step 5: Wait for container to be running
MAX_WAIT=30  # Maximum 30 seconds
WAIT_COUNT=0

set +e
run_with_spinner "Step 5: Waiting for container to start..." "
    while [ \$WAIT_COUNT -lt $MAX_WAIT ]; do
        if docker ps --filter 'name=$CONTAINER_NAME' --format '{{.Names}}' | grep -q '$CONTAINER_NAME'; then
            exit 0
        fi
        sleep 1
        WAIT_COUNT=\$((WAIT_COUNT + 1))
    done
    exit 1
"
WAITING_EXIT_CODE=$?
set -e

if [ $WAITING_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ Step 5: Container is running${NC}"
    sleep 0.5
else
    echo -e "${RED}✗ Step 5: Container failed to start within ${MAX_WAIT}s${NC}"
    echo "Container logs:"
    docker logs "$CONTAINER_NAME" 2>&1 || echo "No logs available"
    exit 1
fi

# Step 6: Test health endpoint
set +e
run_with_spinner "Step 6: Testing health endpoint..." "
    for i in {1..10}; do
        if curl -s http://localhost:$PORT/actuator/health 2>/dev/null | grep -q 'UP'; then
            exit 0
        fi
        sleep 1
    done
    exit 1
"
ENDPOINT_EXIT=$?
set -e

if [ $ENDPOINT_EXIT -eq 0 ]; then
    HEALTH_RESPONSE=$(curl -s http://localhost:$PORT/actuator/health)
    HEALTH_STATUS=$(echo "$HEALTH_RESPONSE" | grep -oP '(?<="status":")[^"]+' | head -1)
    echo -e "${GREEN}✓ Step 6: Health endpoint responding (Status: $HEALTH_STATUS)${NC}"
    sleep 0.5
else
    echo -e "${RED}✗ Step 6: Health endpoint not responding${NC}"
    echo "Tried: http://localhost:$PORT/actuator/health"
    echo "Container may have crashed or actuator not configured. Check logs:"
    echo -e "${YELLOW}docker logs $CONTAINER_NAME${NC}"
    exit 1
fi

# Step 7: Running API endpoint tests
echo ""

# Create a test script that runs all tests
cat > /tmp/run_api_tests.sh << 'TESTEOF'
#!/bin/bash

PORT=$1
STUDENT_ID=$2

# Helper function to test endpoint
test_endpoint() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_code="$5"
    local check_response="$6"
    
    if [ "$method" = "GET" ]; then
        RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:$PORT/api/v1/$endpoint 2>/dev/null)
    else
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" \
            http://localhost:$PORT/api/v1/$endpoint 2>/dev/null)
    fi
    
    if [ $? -ne 0 ]; then
        echo "FAIL|$test_name|Connection failed"
        return 0  # Don't exit script, just continue
    fi
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "$expected_code" ]; then
        if [ -n "$check_response" ]; then
            if echo "$BODY" | grep -q "$check_response"; then
                echo "PASS|$test_name"
            else
                echo "FAIL|$test_name|Response mismatch"
            fi
        else
            echo "PASS|$test_name"
        fi
    else
        echo "FAIL|$test_name|Expected $expected_code, got $HTTP_CODE"
    fi
    
    return 0  # Always return 0 so script continues
}

# Run all tests
test_endpoint "Health check" "GET" "../../actuator/health" "" "200" '"status":"UP"'
test_endpoint "UID endpoint" "GET" "uid" "" "200" "$STUDENT_ID"
test_endpoint "distanceTo (valid)" "POST" "distanceTo" '{"position1": {"lng": -3.192473, "lat": 55.946233}, "position2": {"lng": -3.192473, "lat": 55.942617}}' "200" ""
test_endpoint "distanceTo (invalid)" "POST" "distanceTo" '{"position1": {"lng": -3.192473}}' "400" ""
test_endpoint "isCloseTo (valid)" "POST" "isCloseTo" '{"position1": {"lng": -3.192473, "lat": 55.946233}, "position2": {"lng": -3.192473, "lat": 55.946234}}' "200" ""
test_endpoint "isCloseTo (invalid)" "POST" "isCloseTo" '{"position1": {"lng": -3.192473}}' "400" ""
test_endpoint "nextPosition (valid)" "POST" "nextPosition" '{"start": {"lng": -3.192473, "lat": 55.946233}, "angle": 45}' "200" ""
test_endpoint "nextPosition (invalid)" "POST" "nextPosition" '{"start": {"lng": -3.192473}}' "400" ""
test_endpoint "isInRegion (valid)" "POST" "isInRegion" '{"position": {"lng": -3.188, "lat": 55.944}, "region": {"name": "central", "vertices": [{"lng": -3.192473, "lat": 55.946233}, {"lng": -3.192473, "lat": 55.942617}, {"lng": -3.184319, "lat": 55.942617}, {"lng": -3.184319, "lat": 55.946233}, {"lng": -3.192473, "lat": 55.946233}]}}' "200" ""
test_endpoint "isInRegion (open region)" "POST" "isInRegion" '{"position": {"lng": -3.188, "lat": 55.944}, "region": {"name": "central", "vertices": [{"lng": -3.192473, "lat": 55.946233}, {"lng": -3.192473, "lat": 55.942617}, {"lng": -3.184319, "lat": 55.942617}]}}' "400" ""
test_endpoint "isInRegion (invalid)" "POST" "isInRegion" '{"position": {"lng": -3.188}}' "400" ""

exit 0  # Always exit successfully
TESTEOF

chmod +x /tmp/run_api_tests.sh

# Run tests with spinner
run_with_spinner "Step 7: Running API endpoint tests..." "/tmp/run_api_tests.sh $PORT $STUDENT_ID > /tmp/test_results.txt 2>&1"

# Parse results
echo ""
echo -e "═══════════════════════════════"
echo -e "Test Results                   "
echo -e "═══════════════════════════════"
echo ""

TESTS_PASSED=0
TESTS_FAILED=0

set +e  # Disable exit on error for counting

if [ -f /tmp/test_results.txt ]; then
    while IFS='|' read -r status test_name reason; do
        if [ "$status" = "PASS" ]; then
            echo -e "${GREEN}✓${NC} $test_name"
            TESTS_PASSED=$((TESTS_PASSED + 1))
        elif [ "$status" = "FAIL" ]; then
            echo -e "${RED}✗${NC} $test_name${reason:+ - $reason}"
            TESTS_FAILED=$((TESTS_FAILED + 1))
        fi
    done < /tmp/test_results.txt
fi

set -e  # Re-enable exit on error

echo ""
echo -e "═══════════════════════════════"
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo -e "═══════════════════════════════"
echo ""

echo ""

# Cleanup
echo -e "${YELLOW}Cleaning up...${NC}"

set +e  # Don't exit if cleanup commands fail

# Stop and remove container
if docker ps -a --filter "name=$CONTAINER_NAME" --format '{{.Names}}' | grep -q "$CONTAINER_NAME"; then
    run_with_spinner "Stopping container..." "docker stop $CONTAINER_NAME > /dev/null 2>&1"
    echo -e "${GREEN}✓ Container stopped${NC}"
    
    run_with_spinner "Removing container..." "docker rm $CONTAINER_NAME > /dev/null 2>&1"
    echo -e "${GREEN}✓ Container removed${NC}"
fi

# Remove test image
if docker images ilp_test_image --format '{{.Repository}}' | grep -q ilp_test_image; then
    run_with_spinner "Removing test image..." "docker rmi ilp_test_image > /dev/null 2>&1"
    echo -e "${GREEN}✓ Test image removed${NC}"
fi

# Optional: Clean up dangling images
run_with_spinner "Cleaning dangling images..." "docker image prune -f > /dev/null 2>&1"
echo -e "${GREEN}✓ Cleanup complete${NC}"

set -e

echo ""
echo -e "${GREEN}All done!${NC}"