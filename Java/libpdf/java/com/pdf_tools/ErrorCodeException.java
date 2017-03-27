package com.pdf_tools;

@SuppressWarnings("serial")
public class ErrorCodeException extends Exception
{
    public ErrorCodeException(ErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }

    public ErrorCodeException(ErrorCode errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodeException(ErrorCode errorCode, String message, Throwable cause)
    {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    private ErrorCode errorCode;
}
