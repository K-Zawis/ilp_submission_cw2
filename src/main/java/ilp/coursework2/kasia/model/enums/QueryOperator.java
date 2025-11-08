package ilp.coursework2.kasia.model.enums;

/**
 * Enumeration of comparison operators for drone queries.
 */
public enum QueryOperator {
    EQUALS("="),
    NOT_EQUALS("!="),
    LESS_THAN("<"),
    GREATER_THAN(">");

    private final String symbol;

    QueryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Finds a QueryOperator by its string symbol.
     *
     * @param symbol the operator symbol
     * @return the matching QueryOperator, or null if not found
     */
    public static QueryOperator fromString(String symbol) {
        for (QueryOperator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        return null;
    }
}