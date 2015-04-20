package org.le.Exception;

public class FtlRenderException extends RuntimeException{
    public FtlRenderException(String message){
        super(message);
    }

    public FtlRenderException(Exception e){
        super(e);
    }
}
