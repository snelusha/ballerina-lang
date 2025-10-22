package io.ballerina.fs;

public class StringJoiner {
    private final String delimiter;
    private final String prefix;
    private final String suffix;
    private final StringBuilder builder;
    private String emptyValue;
    private boolean hasElements;

    /**
     * Constructs a CustomStringJoiner with the specified delimiter.
     */
    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    /**
     * Constructs a CustomStringJoiner with the specified delimiter, prefix, and suffix.
     */
    public StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        if (delimiter == null || prefix == null || suffix == null) {
            throw new NullPointerException("delimiter, prefix, or suffix must not be null");
        }
        this.delimiter = delimiter.toString();
        this.prefix = prefix.toString();
        this.suffix = suffix.toString();
        this.builder = new StringBuilder();
        this.hasElements = false;
    }

    /**
     * Sets the string to be used when the joiner is empty.
     */
    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        if (emptyValue == null) {
            throw new NullPointerException("emptyValue must not be null");
        }
        this.emptyValue = emptyValue.toString();
        return this;
    }

    /**
     * Adds a new element to the joiner.
     */
    public StringJoiner add(CharSequence newElement) {
        if (newElement == null) {
            newElement = "null";
        }

        if (hasElements) {
            builder.append(delimiter);
        }
        builder.append(newElement);
        hasElements = true;
        return this;
    }

    /**
     * Merges another CustomStringJoiner into this one.
     */
    public StringJoiner merge(StringJoiner other) {
        if (other == null) {
            throw new NullPointerException("other must not be null");
        }

        if (other.hasElements) {
            if (hasElements) {
                builder.append(delimiter);
            }
            builder.append(other.builder);
            hasElements = true;
        }
        return this;
    }

    /**
     * Returns the length of the final string representation.
     */
    public int length() {
        return toString().length();
    }

    /**
     * Returns the current string representation.
     */
    @Override
    public String toString() {
        if (!hasElements && emptyValue != null) {
            return emptyValue;
        }

        if (prefix.isEmpty() && suffix.isEmpty()) {
            return builder.toString();
        }

        StringBuilder result = new StringBuilder(prefix.length() + builder.length() + suffix.length());
        result.append(prefix);
        result.append(builder);
        result.append(suffix);
        return result.toString();
    }
}