package cs213.photos.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Tag implements Serializable {

    public String value;
    public Type type;

    public Tag(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public enum Type {
        person,
        location;
    }

    @NonNull
    @Override
    public String toString() {
        return "Type: " + type.name() + ", Value: " + value;
    }
}