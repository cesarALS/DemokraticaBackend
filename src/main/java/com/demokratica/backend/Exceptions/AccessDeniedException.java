package com.demokratica.backend.Exceptions;

import java.util.HashMap;

public class AccessDeniedException extends RuntimeException {
    
    public enum Type {UPDATE_SESSION};
    private HashMap<Type, String> message = new HashMap<>() {{
        put(Type.UPDATE_SESSION, "User doesn't have permission to update the session");
    }};
    private Type type;

    public AccessDeniedException (Type type) {
        this.type = type;
    }

    public String getMessage() {
        return this.message.get(this.type);
    }
}
