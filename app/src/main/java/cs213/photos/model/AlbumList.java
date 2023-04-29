package cs213.photos.model;

import java.util.ArrayList;

public class AlbumList {
    public ArrayList<Album> list = new ArrayList<>();

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

    public Album get(String name) {
        if (size == 0) {
            return null;
        }
        return list.stream().filter(p -> p.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Album get(int index) {
        return list.get(index);
    }
}
