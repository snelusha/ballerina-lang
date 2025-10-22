package io.ballerina.fs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public final class Files {

    private Files() {
    }

    public static boolean exists(Path path, LinkOption... options) {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean notExists(Path path, LinkOption... options) {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean isDirectory(Path path, LinkOption... options) {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean isRegularFile(Path path, LinkOption... options) {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean isHidden(Path path, LinkOption... options) {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static byte[] readAllBytes(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static String readString(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static String readString(Path path, Charset charset) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static BufferedReader newBufferedReader(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static BufferedReader newBufferedReader(Path path, Charset charset) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static BufferedWriter newBufferedWriter(Path path, Charset charset, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path createFile(Path path, FileAttribute<?>... attrs) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static void delete(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean deleteIfExists(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static long copy(InputStream in, Path target, CopyOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path move(Path source, Path target, CopyOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<Path> list(Path dir) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<String> lines(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<String> lines(Path path, Charset charset) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path write(Path path, byte[] bytes, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path write(Path path, Iterable<? extends CharSequence> lines, Charset charset, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path writeString(Path path, CharSequence csq, Charset charset, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path writeString(Path path, CharSequence csq, OpenOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Path walkFileTree(Path start, java.util.Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static long size(Path path) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static boolean isSameFile(Path path, Path path2) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        throw new RuntimeException("File operations not supported in web environment");
    }

    public static Stream<Path> find(Path path, int maxValue, Object isBalWithTest) {
        throw new RuntimeException("File operations not supported in web environment");
    }
}
