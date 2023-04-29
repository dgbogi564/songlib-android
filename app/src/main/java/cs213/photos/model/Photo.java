package cs213.photos.model;
;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Photo implements Serializable {

    private static final DateFormat dateFormat = DateFormat.getDateInstance();

    public String filepath;
    public String caption = "";
    public ArrayList<Tag> tags = new ArrayList<>();
    public Calendar date = Calendar.getInstance();

    public Photo(String filepath) throws Exception {
        this.filepath = filepath;
        if (!filepath.startsWith("file:///")) {
            throw new Exception("Not a stock photo.");
        }
        date.set(Calendar.MILLISECOND, 0);
    }

    public Photo(Context context, Uri uri) {
        this.filepath = uri.toString();
        DocumentFile df = DocumentFile.fromSingleUri(context, uri);
        if (df != null) {
            date.setTimeInMillis(df.lastModified());

        }
        date.set(Calendar.MILLISECOND, 0);
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
        return this.filepath.equals(photo.filepath);
    }

    public String getDate() {
        return dateFormat.format(date.getTime());
    }

    @Override
    public String toString() {
        return filepath.substring(Math.max(filepath.lastIndexOf("/"), filepath.lastIndexOf("\\")) + 1);
    }
}