package de.hhn.aib3.aufg3.gruppe11.lobby;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;

/**
 * Adapter for LobbyActivity
 */
public class LobbyAdapter extends BaseAdapter{

    private final List<Game> gameList;
    private final LayoutInflater inflater;

    public LobbyAdapter(Context context, ArrayList<Game> games){

        inflater = LayoutInflater.from(context);
        gameList = new ArrayList<>();

        for(int i=0; i<games.size(); i++){
            gameList.add(games.get(i));
        }
    }

    @Override
    public int getCount() {
        return gameList.size();
    }

    @Override
    public Object getItem(int position) {
        return gameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_lobby, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.text_view_list_name);
            holder.description = convertView.findViewById(R.id.text_view_list_description);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Game game = (Game) getItem(position);
        holder.name.setText(game.getName());
        holder.description.setText(game.getDescription());

        return convertView;
    }

    private static class ViewHolder{
        TextView name;
        TextView description;
    }

}
