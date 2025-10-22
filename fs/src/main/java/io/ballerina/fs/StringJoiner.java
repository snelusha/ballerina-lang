package io.ballerina.fs;

public class StringJoiner {
    private final String delimiter;
    private final String prefix;
    private final String suffix;
    private final StringBuilder builder;
    private String emptyValue;
    private boolean hasElements;

    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

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

    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        if (emptyValue == null) {
            throw new NullPointerException("emptyValue must not be null");
        }
        this.emptyValue = emptyValue.toString();
        return this;
    }

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

    public int length() {
        return toString().length();
    }

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