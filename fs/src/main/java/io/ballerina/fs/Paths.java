package io.ballerina.fs;

public final class Paths {

    private Paths() {
        // Utility class, no instances
    }

    public static Path get(String first, String... more) {
        return Path.of(first, more);
    }

    public static Path get(java.net.URI uri) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
}
