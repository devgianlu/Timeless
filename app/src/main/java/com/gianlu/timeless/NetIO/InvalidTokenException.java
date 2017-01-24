package com.gianlu.timeless.NetIO;

public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
        super("Cannot find the stored access token!");
    }
}
