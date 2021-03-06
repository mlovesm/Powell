package com.green.powell.app.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.green.powell.app.R;
import com.green.powell.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private int resource;
    private ArrayList<HashMap<String,String>> boardList;
    private Context con;
    private String name;

    private CardViewClickListener clickListener;

    public interface CardViewClickListener {
        void onCardClick(int position) ;

    }

    public CheckAdapter(Context con , ArrayList<HashMap<String,String>> array, String name){
        this.con= con;
        boardList = array;
        this.name = name;
    }

    public CheckAdapter(Context con , int resource, ArrayList<HashMap<String,String>> array, String name){
        this.con= con;
        this.resource = resource;
        boardList = array;
        this.name = name;
    }

    public CheckAdapter(Context con , int resource, ArrayList<HashMap<String,String>> array, String name, CardViewClickListener clickListener){
        this.con= con;
        this.resource = resource;
        boardList = array;
        this.name = name;
        this.clickListener = clickListener;
    }

    /**
     * 특정 아이템의 변경사항을 적용하기 위해 기본 아이템을 새로운 아이템으로 변경한다.
     * @param boardList 새로운 아이템
     */
    public void setItem(ArrayList<HashMap<String,String>> boardList) {
        boardList.get(0);
    }

    /**
     * 현재 아이템 리스트에 새로운 아이템 리스트를 추가한다.
     * @param boardList 새로운 아이템 리스트
     */
    public void addItemList(ArrayList<HashMap<String,String>> boardList) {
        this.boardList.addAll(boardList);
        notifyDataSetChanged();
    }


    /**
     * 아이템 크기를 반환한다.
     * @return 아이템 크기
     */
    @Override
    public int getItemCount() {
        return this.boardList.size();
    }

    /**
     * 뷰홀더(ViewHolder)를 생성하기 위해 자동으로 호출된다.
     * @param parent 부모 뷰그룹
     * @param viewType 새로운 뷰의 뷰타입
     * @return 뷰홀더 객체
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        return new ViewHolder(v);
    }

    /**
     * 뷰홀더(ViewHolder)와 아이템을 리스트 위치에 따라 연동한다.
     * @param holder 뷰홀더 객체
     * @param pos 리스트 위치
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int position= pos;
        holder.board_data1.setText(boardList.get(position).get("data1"));
        holder.board_data2.setText(boardList.get(position).get("data2"));
        holder.board_data3.setText(boardList.get(position).get("data3"));

        if(name.equals("Check")){
            String checkState= boardList.get(position).get("data4");
            if(checkState.equals("1")){
                holder.board_data4.setText("양호");
                holder.board_data4.setBackgroundResource(R.drawable.box_green);
            }else if(checkState.equals("2")){
                holder.board_data4.setText("이상");
                holder.board_data4.setBackgroundResource(R.drawable.box_red);
            }else {
                holder.board_data4.setText("이상");
                holder.board_data4.setBackgroundResource(R.drawable.box_red);
            }

            holder.board_data4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }else if(name.equals("UnCheck")){
            String type_kor= boardList.get(position).get("data5");
            holder.board_data3.setText(boardList.get(position).get("data3")+type_kor);
            holder.board_data4.setText(boardList.get(position).get("data4")+type_kor);

        }else{

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onCardClick(position);

                }
            }
        });

    }


    /**
     * 아이템을 보여주기 위한 뷰홀더 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView board_data1;
        TextView board_data2;
        TextView board_data3;
        TextView board_data4;
        TextView board_data5;
        TextView board_data6;
        Button button1;
        Button button2;

        public ViewHolder(View v) {
            super(v);

            board_data1 = (TextView)v.findViewById(R.id.textView1);
            board_data2 = (TextView)v.findViewById(R.id.textView2);
            board_data3 = (TextView)v.findViewById(R.id.textView3);
            board_data4 = (TextView)v.findViewById(R.id.textView4);

            button1 = (Button) v.findViewById(R.id.button1);
            button2 = (Button) v.findViewById(R.id.button2);
        }
    }
}
