package cs213.photos.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Photo implements Serializable {

    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    public Uri uri;
    public Drawable drawable;
    public String caption = "";
    public ArrayList<Tag> tags = new ArrayList<>();
    public Calendar date;

    public Photo(String filepath) throws Exception {
        File file = new File(filepath);
        if (!file.exists()) {
            throw new Exception("Photo does not exist.");
        }
        this.uri = Uri.parse(filepath);
        this.drawable = Drawable.createFromStream(Files.newInputStream(file.toPath()), null);
        date = Calendar.getInstance();
        date.setTimeInMillis(file.lastModified());
        date.set(Calendar.MILLISECOND, 0);
    }

    public Photo(Uri uri, InputStream is) {
        this.uri = uri;
        this.drawable = Drawable.createFromStream(is, null);
        date = Calendar.getInstance();
    }

    public void addTag(Tag.Type type, String value) throws Exception {
        if (getTag(type, value) != null) {
            throw new Exception("Tag already exists.");
        }
        tags.add(new Tag(type, value));
    }

    public Tag getTag(Tag.Type type, String value) {
        return tags.stream().filter(t -> t.type == type && t.value.equals(value)).findFirst().orElse(null);
    }

    public void modifyTag(Tag.Type type, String fromValue, String toValue) throws Exception {
        Tag tag = getTag(type, fromValue);
        if (tag == null) {
            throw new Exception("Tag doesn't exist.");
        }
        tag.value = toValue;
    }

    public void removeTag(Tag.Type type, String value) throws Exception {
        Tag tag = getTag(type, value);
        if (tag == null) {
            throw new Exception("Tag doesn't exist.");
        }
        tags.remove(tag);
    }

    public void addCaption(String caption) {
        this.caption = caption;
    }

    public void removeCaption() {
        this.caption = "";
    }

    public boolean equals(Photo photo) {
        return this.uri.equals(photo.uri);
    }

    public String getDate() {
        return dateFormat.format(date.getTime());
    }
}