package cs213.photos.model;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Album implements Serializable {

    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    private final Calendar earliestDate = Calendar.getInstance();
    private final Calendar latestDate = Calendar.getInstance();
    public String name;
    public ArrayList<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
        latestDate.setTimeInMillis(0);
        calculateTimeStats();
    }

    public void add(String filepath) throws Exception {
        if (get(filepath) != null) {
            throw new Exception("Photo already exists.");
        }
        photos.add(new Photo(filepath));
        calculateTimeStats();
    }

    public void add(Context context, Uri uri) throws Exception {
        if (get(uri.toString()) != null) {
            throw new Exception("Photo already exists.");
        }
        photos.add(new Photo(context, uri));
        calculateTimeStats();
    }

    public void add(Photo photo) {
        photos.add(photo);
    }

    public void remove(Photo photo) {
        if (photos.size() == 0) {
            return;
        }
        this.photos.remove(photo);
        calculateTimeStats();
    }

    public void remove(int pos) {
        if (photos.size() == 0) {
            return;
        }
        this.photos.remove(pos);
        calculateTimeStats();
    }

    public Photo get(String filepath) {
        return photos.stream().filter(p -> p.filepath.equals(filepath)).findFirst().orElse(null);
    }

    public Photo get(int index) {
        return photos.get(index);
    }

    public void calculateTimeStats() {
        if (photos.size() == 0) {
            latestDate.setTimeInMillis(0);
            earliestDate.setTimeInMillis(Long.MAX_VALUE);
            earliestDate.set(Calendar.MILLISECOND, 0);
            return;
        }

        for (Photo photo : photos) {
            long lastModified = photo.date.getTimeInMillis();
            if (lastModified < earliestDate.getTimeInMillis()) {
                earliestDate.setTimeInMillis(lastModified);
            }
            if (lastModified > latestDate.getTimeInMillis()) {
                latestDate.setTimeInMillis(lastModified);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return photos.size();
    }

    public String getEarliestDate() {
        return dateFormat.format(earliestDate.getTime());
    }

    public String getLatestDate() {
        return dateFormat.format(latestDate.getTime());
    }
}