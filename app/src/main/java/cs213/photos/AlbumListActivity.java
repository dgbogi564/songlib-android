package cs213.photos;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import cs213.photos.model.Album;
import cs213.photos.model.AlbumList;

public class AlbumListActivity extends AppCompatActivity {

    private static AlbumList albumList;
    private ListView listView;
    private static final String saveLocation = "photos.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load/create album
        try (FileInputStream fis = openFileInput(saveLocation);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            albumList = (cs213.photos.model.AlbumList) ois.readObject();
            albumList.remove("Stock");
        } catch (IOException | ClassNotFoundException e) {
            albumList = new AlbumList();
        }

        // Setup stock album
        Album album = new Album("Stock");
        try {
            AssetManager assets = getAssets();
            for (String path : assets.list("photos")) {

                album.add(Uri.parse("file:///android_asset/photos/" + path),
                        assets.open("photos/" + path));
            }
            albumList.add(album);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Setup list view
        listView = findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.album_list_item, albumList.list));
        listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));
    }

    private void showAlbum(int pos) {
        Album.currentAlbum = albumList.get(pos);
        startActivity(new Intent(this, AlbumActivity.class));
    }

    private void addAlbum() {
        Dialog dialog = new Dialog((AlbumListActivity.this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_album);

        EditText albumName = dialog.findViewById(R.id.add_album_name);
        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    albumList.add(new Album(albumName.getText().toString()));
                    dialog.dismiss();
                } catch (Exception e) {
                    errorText.setText(e.getMessage());
                }
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}