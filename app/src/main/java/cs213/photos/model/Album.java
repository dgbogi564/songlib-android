package cs213.photos.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Album implements Serializable {

    private static final DateFormat dateFormat = DateFormat.getDateInstance();
    public static Album currentAlbum;
    public String name;
    public ArrayList<Photo> photos;
    Calendar earliestDate = Calendar.getInstance();
    Calendar latestDate = Calendar.getInstance();
    private int size;

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
        this.photos.add(new Photo(filepath));
        size++;
        calculateTimeStats();
    }

    public void remove(Photo photo) {
        this.photos.remove(photo);
        size--;
        calculateTimeStats();
    }

    public Photo get(String filepath) {
        return photos.stream().filter(p -> p.filepath.equals(filepath)).findFirst().orElse(null);
    }

    public Photo get(Uri uri) {
        return photos.stream().filter(p -> p.filepath.equals(uri.toString())).findFirst().orElse(null);
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
        return size;
    }

    public String getEarliestDate() {
        return dateFormat.format(earliestDate.getTime());
    }

    public String getLatestDate() {
        return dateFormat.format(latestDate.getTime());
    }
}