package cs213.photos;

import static cs213.photos.model.ErrorHandling.alertDialog;
import static cs213.photos.model.State.currentAlbum;
import static cs213.photos.model.State.currentAlbumList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicInteger;

import cs213.photos.model.Album;
import cs213.photos.model.AlbumList;
import cs213.photos.model.Photo;
import cs213.photos.model.State;

public class AlbumActivity extends AppCompatActivity implements AlbumFragment.OnListFragmentInteractionListener {

    public static Album album;
    public static AlbumList albumList;
    public static boolean isResult = false;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    ActivityResultLauncher<Intent> photoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String filepath = null;
                            try {
                                Uri uri = data.getData();
                                getContentResolver().takePersistableUriPermission(uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                album.add(AlbumActivity.this, uri);
                                recyclerViewAdapter.notifyItemInserted(album.getSize() - 1);
                                albumList.save(AlbumActivity.this);
                            } catch (Exception e) {
                                alertDialog(AlbumActivity.this, e);
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        album = currentAlbum;
        albumList = currentAlbumList;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup recycler view
        if (recyclerViewAdapter == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
            recyclerView = (RecyclerView) fragment.getView();
            recyclerViewAdapter = recyclerView.getAdapter();
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                    DividerItemDecoration.VERTICAL));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.save_as_album).setVisible(isResult);
        menu.findItem(R.id.add).setVisible(!isResult);
        menu.findItem(R.id.delete).setVisible(!isResult);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Photo photo) {
        State.currentPhoto = photo;
        startActivity(new Intent(this, PhotoActivity.class));
    }

    private void addPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        photoResultLauncher.launch(intent);
    }

    private void deletePhoto() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.delete_photo);

        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);
        Spinner spinner = dialog.findViewById(R.id.spinner);

        AtomicInteger idx = new AtomicInteger();


        // Initialize spinner
        ArrayAdapter<Photo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, album.photos);
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                album.remove(i);
                albumList.save(getApplicationContext());
                recyclerViewAdapter.notifyItemRangeRemoved(i, 1);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveAsAlbum() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.save_as_album);

        EditText albumName = dialog.findViewById(R.id.edit_text);
        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    album.name = albumName.getText().toString();
                    albumList.add(album);
                    albumList.save(getApplicationContext());
                    dialog.dismiss();
                } catch (Exception e) {
                    errorText.setText(e.getMessage());
                }
            }
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add) {
            addPhoto();
            return true;
        }
        if (itemId == R.id.delete) {
            deletePhoto();
            return true;
        }
        if (item.getItemId() == R.id.save_as_album) {
            saveAsAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
