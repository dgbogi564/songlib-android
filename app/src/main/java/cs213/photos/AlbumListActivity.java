package cs213.photos;

import static cs213.photos.model.State.currentAlbum;
import static cs213.photos.model.State.currentAlbumList;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.atomic.AtomicInteger;

import cs213.photos.model.Album;
import cs213.photos.model.AlbumList;
import cs213.photos.model.Photo;
import cs213.photos.model.Tag;
import cs213.photos.model.Tag.Type;

public class AlbumListActivity extends AppCompatActivity {
    public static final String saveLocation = "photos.dat";
    ListView listView;
    ArrayAdapter<Album> listAdapter;
    private AlbumList albumList;

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
            for (Photo photo : album.photos) {
                photo.addTag(Type.location, "locationTest");
                photo.addTag(Type.person, "personTest");
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

    @Override
    public void onResume() {
        super.onResume();
        listAdapter.notifyDataSetChanged();
    }

    private void showAlbum(int pos) {
        currentAlbum = albumList.get(pos);
        AlbumActivity.isResult = false;
        startActivity(new Intent(this, AlbumActivity.class));
    }

    private void search() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.search_album);

        EditText value1_et = dialog.findViewById(R.id.edit_text_1);
        EditText value2_et = dialog.findViewById(R.id.edit_text_2);
        Spinner spinner1 = dialog.findViewById(R.id.spinner_1);
        Spinner spinner2 = dialog.findViewById(R.id.spinner_2);
        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);
        CheckBox checkBox = dialog.findViewById(R.id.checkbox);

        // Initialize spinners

        AtomicInteger idx1 = new AtomicInteger();
        AtomicInteger idx2 = new AtomicInteger();

        ArrayAdapter<Type> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Type.values());
        ArrayAdapter<Type> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Type.values());
        adapter1.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (spinner2.isSelected()) {
                    submitButton.setEnabled(true);
                }
                idx1.set(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                submitButton.setEnabled(false);
            }
        });
        spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (spinner1.isSelected()) {
                    submitButton.setEnabled(true);
                }
                idx2.set(pos);
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
                String value1 = value1_et.getText().toString().trim();
                String value2 = value2_et.getText().toString().trim();
                if (value1.length() == 0 || value2.length() == 0) {
                    errorText.setText("Value fields not filled.");
                }

                // Gather search results
                Album searchResult = new Album("searchResults");
                if (checkBox.isChecked()) {
                    // Conjunctive search
                    for (Album album : albumList.list) {
                        for (Photo photo : album.photos) {
                            if (searchResult.photos.contains(photo)) {
                                continue;
                            }
                            boolean match1 = false;
                            boolean match2 = false;
                            for (Tag tag : photo.tags) {
                                if (Type.values()[idx1.get()] == tag.type) {
                                    if (tag.value.equalsIgnoreCase(value1)) {
                                        match1 = true;
                                    }
                                }
                                if (Type.values()[idx2.get()] == tag.type) {
                                    if (tag.value.equalsIgnoreCase(value2)) {
                                        match2 = true;
                                    }
                                }
                            }
                            if (match1 && match2) {
                                searchResult.add(photo);
                            }
                        }
                    }
                } else {
                    // Disjunctive search
                    for (Album album : albumList.list) {
                        for (Photo photo : album.photos) {
                            if (searchResult.photos.contains(photo)) {
                                continue;
                            }
                            for (Tag tag : photo.tags) {
                                if (Type.values()[idx1.get()] == tag.type) {
                                    if (tag.value.equalsIgnoreCase(value1)) {
                                        searchResult.add(photo);
                                        break;
                                    }
                                }
                                if (Type.values()[idx2.get()] == tag.type) {
                                    if (tag.value.equalsIgnoreCase(value2)) {
                                        searchResult.add(photo);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                currentAlbum = searchResult;
                AlbumActivity.isResult = true;
                dialog.dismiss();
                startActivity(new Intent(AlbumListActivity.this, AlbumActivity.class));
            }
        });

        dialog.show();
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

    private void renameAlbum() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.rename_album);

        Button submitButton = dialog.findViewById(R.id.submit_button);
        EditText albumName = dialog.findViewById(R.id.edit_text);
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
                String name = albumName.getText().toString();
                Album album = albumList.get(i);
                if (albumList.get(name) != null) {
                    errorText.setText("Album with same name already exists");
                    return;
                }
                album.name = name;
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
        if (itemId == R.id.rename) {
            renameAlbum();
            return true;
        }
        if (itemId == R.id.search) {
            search();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}