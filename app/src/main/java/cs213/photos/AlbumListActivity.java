package cs213.photos;

import static cs213.photos.model.ErrorHandling.alertDialog;
import static cs213.photos.model.State.currentAlbumList;
import static cs213.photos.model.State.currentAlbum;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import cs213.photos.model.Album;
import cs213.photos.model.AlbumList;

public class AlbumListActivity extends AppCompatActivity {
    private ListView listView;

    private ArrayAdapter<Album> listAdapter;
    private AlbumList albumList;

    public static final String saveLocation = "photos.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        albumList = currentAlbumList = AlbumList.load(getApplicationContext());

        // Setup stock album
        Album album = new Album("Stock");
        try {
            AssetManager assets = getAssets();
            for (String path : assets.list("photos")) {
                album.add("file:///android_asset/photos/" + path);
            }
            albumList.add(album);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Setup list view
        listView = findViewById(R.id.listView);
        listAdapter = new ArrayAdapter<>(this, R.layout.album_list_item, albumList.list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));
    }

    private void showAlbum(int pos) {
        currentAlbum = albumList.get(pos);
        startActivity(new Intent(this, AlbumActivity.class));
    }

    private void addAlbum() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_album);

        EditText albumName = dialog.findViewById(R.id.edit_text);
        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    albumList.add(new Album(albumName.getText().toString()));
                    listAdapter.notifyDataSetChanged();
                    albumList.save(getApplicationContext());
                    dialog.dismiss();
                } catch (Exception e) {
                    errorText.setText(e.getMessage());
                }
            }
        });

        dialog.show();

    }

    private void deleteAlbum() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.delete_album);

        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);
        Spinner spinner = dialog.findViewById(R.id.spinner);

        AtomicInteger idx = new AtomicInteger();


        // Initialize spinner
        ArrayAdapter<Album> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, albumList.list);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                submitButton.setEnabled(true);
                idx.set(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                submitButton.setEnabled(false);
            }
        });

        // Initialize submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = idx.get();
                albumList.remove(i);
                albumList.save(getApplicationContext());
                listAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add) {
            addAlbum();
            return true;
        }
        if (itemId == R.id.delete) {
            deleteAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}