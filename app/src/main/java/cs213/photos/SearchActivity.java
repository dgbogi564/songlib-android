package cs213.photos;

import static cs213.photos.model.ErrorHandling.alertDialog;
import static cs213.photos.model.State.currentAlbum;
import static cs213.photos.model.State.currentAlbumList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

import cs213.photos.model.Album;
import cs213.photos.model.AlbumList;
import cs213.photos.model.Photo;

public class SearchActivity extends AppCompatActivity implements AlbumFragment.OnListFragmentInteractionListener {

    public static Album album;
    public static AlbumList albumList;
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
                                album.add(SearchActivity.this, data.getData());
                                recyclerViewAdapter.notifyItemInserted(album.getSize() - 1);
                            } catch (Exception e) {
                                alertDialog(SearchActivity.this, e);
                            }
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
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
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
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
        if (item.getItemId() == R.id.save_as_album) {
            saveAsAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Photo item) {
        // This is where you'd handle clicking an item in the list
    }
}
