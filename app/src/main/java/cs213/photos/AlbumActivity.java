package cs213.photos;

import static cs213.photos.model.ErrorHandling.alertDialog;
import static cs213.photos.model.State.currentAlbum;
import static cs213.photos.model.State.currentAlbumList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicInteger;

import cs213.photos.model.Album;
import cs213.photos.model.Photo;

public class AlbumActivity extends AppCompatActivity implements AlbumFragment.OnListFragmentInteractionListener {

    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    public static Album album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        album = currentAlbum;
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
    public void onListFragmentInteraction(Photo item) {
        // This is where you'd handle clicking an item in the list
    }

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
                                album.add(AlbumActivity.this, data.getData());
                                recyclerViewAdapter.notifyItemInserted(album.getSize() - 1);
                            } catch (Exception e) {
                                alertDialog(AlbumActivity.this, e);
                            }
                        }
                    }
                }
            });

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
                currentAlbumList.save(getApplicationContext());
                recyclerViewAdapter.notifyItemRangeRemoved(i, 1);
                dialog.dismiss();
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
        return super.onOptionsItemSelected(item);
    }
}
