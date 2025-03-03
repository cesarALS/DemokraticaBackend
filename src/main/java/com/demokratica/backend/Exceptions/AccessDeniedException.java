package com.demokratica.backend.Exceptions;

import java.util.HashMap;

public class AccessDeniedException extends RuntimeException {
    
    public enum Type {UPDATE_SESSION, CREATE_ACTIVITY, PARTICIPATE_IN_ACTIVITY};
    private HashMap<Type, String> message = new HashMap<>() {{
        put(Type.UPDATE_SESSION, "User doesn't have permission to update the session");
        put(Type.CREATE_ACTIVITY, "User doesn't have permission to create activities");
        put(Type.PARTICIPATE_IN_ACTIVITY, "User doesn't have permission to participate in the activities: not invited to session");
    }};
    private Type type;

    public AccessDeniedException (Type type) {
        this.type = type;
    }

    public String getMessage() {
        return this.message.get(this.type);
    }
}
