package cs213.photos;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;

import cs213.photos.AlbumFragment.OnListFragmentInteractionListener;
import cs213.photos.model.Album;
import cs213.photos.model.ErrorHandling;
import cs213.photos.model.Photo;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Photo}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private final Album album;
    private final OnListFragmentInteractionListener listener;

    public AlbumAdapter(Album album, OnListFragmentInteractionListener listener) {
        this.album = album;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Photo photo = album.get(position);
        String path = photo.filepath.startsWith("file:///")
                ? photo.filepath.substring(22)
                : photo.filepath;
        Context context = holder.imageView.getContext();

        // Set holder values
        holder.photo = photo;
        holder.name_text_view.setText(photo.toString());
        holder.dateView.setText(photo.getDate());

        if (path.startsWith("content://")) {
            Uri uri = Uri.parse(path);
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                holder.imageView.setImageDrawable(Drawable.createFromStream(is, null));
            } catch (IOException e) {
                ErrorHandling.alertDialog(context, e);
                return;
            }
        } else {
            try (InputStream is = context.getAssets().open(path)) {
                holder.imageView.setImageDrawable(Drawable.createFromStream(is, null));
            } catch (IOException e) {
                ErrorHandling.alertDialog(context, e);
                return;
            }
        }

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
        public final TextView name_text_view;
        public Photo photo;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imageView = view.findViewById(R.id.image_view);
            dateView = view.findViewById(R.id.date_text_view);
            name_text_view = view.findViewById(R.id.name_text_view);
        }
    }
}