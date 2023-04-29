package cs213.photos;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import cs213.photos.AlbumFragment.OnListFragmentInteractionListener;
import cs213.photos.model.Album;
import cs213.photos.model.Photo;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Photo}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private final Album album;

    private final Context context;
    private final OnListFragmentInteractionListener listener;

    public AlbumAdapter(Album album, Context context, OnListFragmentInteractionListener listener) {
        this.album = album;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Photo photo = album.get(position);

        holder.photo = photo;
        Drawable drawable;
        if (photo.filepath.startsWith("file:///")) {

            try (InputStream is = context.getAssets().open(photo.filepath.substring(22))) {
                drawable = Drawable.createFromStream(is, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File file = new File(photo.filepath);
            try (InputStream is = Files.newInputStream(file.toPath())) {
                drawable = Drawable.createFromStream(is, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        holder.imageView.setImageDrawable(drawable);
        holder.dateView.setText(photo.getDate());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder.photo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return album.getSize();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageView imageView;
        public final TextView dateView;
        public Photo photo;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imageView = view.findViewById(R.id.image_view);
            dateView = view.findViewById(R.id.date_text_view);
        }
    }
}