package cs213.photos;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import cs213.photos.model.Photo;

public class AlbumActivity extends AppCompatActivity implements AlbumFragment.OnListFragmentInteractionListener {

    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
