package com.pdf_tools;

public enum ErrorCode
{
    GENERIC(10),
    FATAL(11),
    LICENSE(12),
    NOT_FOUND(13),
    IO (14),
    UNKNOWN_FORMAT(15),
    CORRUPT(16),
    PASSWORD(17),
    CONFORMANCE(18);

    ErrorCode(int value)
    {
        this.value = value;
    }

    public static ErrorCode fromValue(int value)
    {
        switch (value)
        {
            case 10: return GENERIC;
            case 11: return FATAL;
            case 12: return LICENSE;
            case 13: return NOT_FOUND;
            case 14: return IO;
            case 15: return UNKNOWN_FORMAT;
            case 16: return CORRUPT;
            case 17: return PASSWORD;
            case 18: return CONFORMANCE;
        }
        throw new IllegalArgumentException("Unknown error code " + value);
    }

    int getValue()
    {
        return value;
    }

    int value;
}
