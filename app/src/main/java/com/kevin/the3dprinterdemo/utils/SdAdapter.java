package com.kevin.the3dprinterdemo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevin.the3dprinterdemo.R;

import java.util.List;

/**
 * Created by Administrator
 * on 2016/9/18.
 */
public class SdAdapter extends BaseAdapter {


    private List<String> mlist;
    private Context mContext;

    public SdAdapter(Context context, List<String> list) {
        mlist = list;
        mContext = context;
    }


    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.sd_list_item_layout, null);
            viewHolder.tvPath = (TextView) view.findViewById(R.id.tvPath);
            viewHolder.imageFile = (ImageView) view.findViewById(R.id.imageFile);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String path = (String)getItem(position);
        viewHolder.tvPath.setText(path);
        if(!path.equals("...")){
            if(path.substring(path.length()-1,path.length()).equals("/")){
                viewHolder.imageFile.setImageResource(R.drawable.folder);
            }else{
                viewHolder.imageFile.setImageResource(R.drawable.file);
            }
        }else{
            viewHolder.imageFile.setImageDrawable(null);
        }


        return view;
    }


    private class ViewHolder {
        private TextView tvPath;
        private ImageView imageFile;
    }

}

