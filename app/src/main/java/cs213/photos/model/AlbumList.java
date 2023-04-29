package cs213.photos.model;

import static cs213.photos.model.ErrorHandling.alertDialog;

import android.content.Context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class AlbumList implements Serializable {
    public ArrayList<Album> list = new ArrayList<>();

    private static final String saveLocation = "photos.dat";

    public int size = 0;

    public void add(Album album) throws Exception {
        if (get(album.name) != null) {
            throw new Exception("Album with the same name already exists.");
        }
        list.add(album);
        size++;
    }

    public void remove(Album album) {
        if (size == 0) {
            return;
        }
        this.list.remove(album);
        size--;
    }

    public void remove(String name) {
        if (size == 0) {
            return;
        }
        this.list.remove(get(name));
        size--;
    }

    public void remove(int pos) {
        if (size == 0) {
            return;
        }
        this.list.remove(pos);
        size--;
    }

    public Album get(String name) {
        if (size == 0) {
            return null;
        }
        return list.stream().filter(p -> p.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Album get(int index) {
        return list.get(index);
    }

    public void save(Context context) {
        try (ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(saveLocation, Context.MODE_PRIVATE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            alertDialog(context, e);
        }
    }

    public static AlbumList load(Context context) {
        AlbumList albumList;
        try (ObjectInputStream ois = new ObjectInputStream(context.openFileInput(saveLocation))) {
            albumList = (cs213.photos.model.AlbumList) ois.readObject();
            albumList.remove("Stock");
        } catch (IOException | ClassNotFoundException e) {
            albumList = new AlbumList();
        }
        return albumList;
    }


}
