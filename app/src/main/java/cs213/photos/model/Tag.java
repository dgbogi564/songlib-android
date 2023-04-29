package cs213.photos.model;

import java.io.Serializable;

public class Tag implements Serializable {

    public String value;
    public Type type;

    public Tag(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    enum Type {
        person,
        location
    }
}