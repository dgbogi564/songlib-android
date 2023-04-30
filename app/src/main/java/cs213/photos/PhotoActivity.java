package cs213.photos;

import static cs213.photos.model.State.currentAlbumList;
import static cs213.photos.model.State.currentPhoto;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import cs213.photos.model.ErrorHandling;
import cs213.photos.model.Photo;
import cs213.photos.model.Tag;
import cs213.photos.model.Tag.Type;

public class PhotoActivity extends AppCompatActivity {
    private ImageView imageView;

    private ListView listView;

    private ArrayAdapter<Tag> listAdapter;

    Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup content view and toolbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photo = currentPhoto;

        // Setup image view
        imageView = findViewById(R.id.image_view);
        String path = photo.filepath.startsWith("file:///")
                ? photo.filepath.substring(22)
                : photo.filepath;
        if (path.startsWith("content://")) {
            try (InputStream is = getContentResolver().openInputStream(Uri.parse(path))) {
                imageView.setImageDrawable(Drawable.createFromStream(is, null));
            } catch (IOException e) {
                ErrorHandling.alertDialog(this, e);
                return;
            }
        } else {
            try (InputStream is = getAssets().open(path)) {
                imageView.setImageDrawable(Drawable.createFromStream(is, null));
            } catch (IOException e) {
                ErrorHandling.alertDialog(this, e);
                return;
            }
        }

        // Setup list view
        listView = findViewById(R.id.list_view);
        listAdapter = new ArrayAdapter<>(this, R.layout.album_list_item, photo.tags);
        listView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_menu, menu);
        return true;
    }

    private void addTag() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.rename_album);

        Button submitButton = dialog.findViewById(R.id.submit_button);
        EditText value_et = dialog.findViewById(R.id.edit_text);
        TextView errorText = dialog.findViewById(R.id.error_text);
        Spinner spinner = dialog.findViewById(R.id.spinner);

        AtomicInteger idx = new AtomicInteger();

        // Initialize spinner
        ArrayAdapter<Type> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Type.values());
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
                String value = value_et.toString().trim();
                if (value.length() == 0) {
                    errorText.setText("Value field not filled.");
                }
                Type type = Type.values()[idx.get()];

                if (photo.getTag(type, value) != null) {
                    errorText.setText("Tag with same type and value already exists");
                    return;
                }

                try {
                    photo.addTag(type, value);
                } catch (Exception e) {
                    ErrorHandling.alertDialog(PhotoActivity.this, e);
                }

                currentAlbumList.save(getApplicationContext());
                listAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteTag() {
        Dialog dialog = new Dialog((this));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.delete_tag);

        Button submitButton = dialog.findViewById(R.id.submit_button);
        TextView errorText = dialog.findViewById(R.id.error_text);
        Spinner spinner = dialog.findViewById(R.id.spinner);

        AtomicInteger idx = new AtomicInteger();


        // Initialize spinner
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, photo.tags);
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
                photo.tags.remove(i);
                currentAlbumList.save(getApplicationContext());
                listAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.add) {
            addTag();
            return true;
        }
        if (itemId == R.id.delete) {
            deleteTag();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}