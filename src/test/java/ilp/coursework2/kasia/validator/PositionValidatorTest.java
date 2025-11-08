package ilp.coursework2.kasia.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;

@DisplayName("Position Validator Tests")
class PositionValidatorTest {

    private PositionValidator positionValidator;

    @BeforeEach
    void setUp() {
        positionValidator = new PositionValidator();
    }

    @Test
    @DisplayName("Valid position with lng and lat returns true")
    void validPosition() {
        Position position = new Position(-3.192473, 55.946233);

        boolean result = positionValidator.isValidPosition(position);

        assertTrue(result);
    }

    @Test
    @DisplayName("Null position returns false")
    void nullPosition() {
        boolean result = positionValidator.isValidPosition(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Position with null longitude returns false")
    void nullLongitude() {
        Position position = new Position(null, 55.946233);

        boolean result = positionValidator.isValidPosition(position);

        assertFalse(result);
    }

    @Test
    @DisplayName("Position with null latitude returns false")
    void nullLatitude() {
        Position position = new Position(-3.192473, null);

        boolean result = positionValidator.isValidPosition(position);

        assertFalse(result);
    }

    @Test
    @DisplayName("Valid position pair returns true")
    void validPositionPair() {
        PositionPair pair = new PositionPair(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617));

        boolean result = positionValidator.isValidPositionPair(pair);

        assertTrue(result);
    }

    @Test
    @DisplayName("Null position pair returns false")
    void nullPositionPair() {
        boolean result = positionValidator.isValidPositionPair(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Position pair with null first position returns false")
    void nullFirstPosition() {
        PositionPair pair = new PositionPair(
                null,
                new Position(-3.192473, 55.942617));

        boolean result = positionValidator.isValidPositionPair(pair);

        assertFalse(result);
    }

    @Test
    @DisplayName("Position pair with null second position returns false")
    void nullSecondPosition() {
        PositionPair pair = new PositionPair(
                new Position(-3.192473, 55.946233),
                null);

        boolean result = positionValidator.isValidPositionPair(pair);

        assertFalse(result);
    }

    @Test
    @DisplayName("Valid next position request returns true")
    void validNextPositionRequest() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                45.0);

        boolean result = positionValidator.isValidNextPositionRequest(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Null next position request returns false")
    void nullNextPositionRequest() {
        boolean result = positionValidator.isValidNextPositionRequest(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Next position request with null start position returns false")
    void nullStartPosition() {
        NextPositionRequest request = new NextPositionRequest(null, 45.0);

        boolean result = positionValidator.isValidNextPositionRequest(request);

        assertFalse(result);
    }

    @Test
    @DisplayName("Next position request with null angle returns false")
    void nullAngle() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                null);

        boolean result = positionValidator.isValidNextPositionRequest(request);

        assertFalse(result);
    }
}