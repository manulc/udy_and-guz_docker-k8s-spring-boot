package org.mlorenzo.springcloud.msvc.usuarios.exceptions;


public class EmailExistsException extends RuntimeException {

    public EmailExistsException(String message) {
        super(message);
    }
}
