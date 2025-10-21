package io.ballerina.fs;

class TFile implements File {

    TFile() {
    }

    @Override
    public long length() {
        throw new RuntimeException("File operations not supported in web environment");
    }
}