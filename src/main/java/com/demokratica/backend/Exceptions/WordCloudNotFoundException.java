package com.demokratica.backend.Exceptions;

public class WordCloudNotFoundException extends RuntimeException {
    
    public WordCloudNotFoundException (Long id) {
        super("Couldn't find wordcloud with the id" + String.valueOf(id));
    }
}
