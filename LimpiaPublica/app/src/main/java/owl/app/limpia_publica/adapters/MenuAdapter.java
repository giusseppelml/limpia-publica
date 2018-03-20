package owl.app.limpia_publica.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import owl.app.limpia_publica.R;
import owl.app.limpia_publica.activities.MainActivity;
import owl.app.limpia_publica.models.Opciones;

/**
 * Created by giusseppe on 04/03/2018.
 */

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>{
    private List<Opciones> menuList;
    private int layout;
    private MenuOnItemClickListener itemClickListener;

    private Context context;

    public MenuAdapter(List<Opciones> menuList, int layout, MenuOnItemClickListener listener) {
        this.menuList = menuList;
        this.layout = layout;
        this.itemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(menuList.get(position), itemClickListener);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView TextViewTitulo;
        public ImageView ImageViewImagen;
        //public CircleImageView circleImageView;

        public ViewHolder(View v) {
            super(v);
            TextViewTitulo = (TextView) itemView.findViewById(R.id.cardViewcmMenuTitulo);
            ImageViewImagen = (ImageView) itemView.findViewById(R.id.cardViewMenuImagen);
            //circleImageView = (CircleImageView) itemView.findViewById(R.id.cardViewMenuCircleImagen);
        }

        public void bind(final Opciones menu, final MenuOnItemClickListener listener) {
            //procesamos los datos a renderizar

            TextViewTitulo.setText(menu.getNombre());
            Picasso.with(context).load(menu.getImagen()).fit().into(ImageViewImagen);
            //Picasso.with(context).load(menu.getImagen()).fit().into(circleImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ... pasamos nuestro objeto modelo (este caso String) y posición
                    listener.onItemClick(menu, getAdapterPosition());
                }
            });
        }
    }
}