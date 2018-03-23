package com.green.powell.app.adaptor;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.green.powell.app.R;
import com.green.powell.app.check.CheckWriteFragment;
import com.green.powell.app.util.AnimatedExpandableListView;
import com.green.powell.app.util.ExpandedChildModel;
import com.green.powell.app.util.ExpandedMenuModel;

import java.util.HashMap;
import java.util.List;

public class AnyExpandableAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private static class GroupHolder {
        TextView title;
        TextView state;
    }

    private static class ChildHolder {
        EditText etc;
        RadioGroup state;
    }

//    private LayoutInflater inflater;
    private static final String TAG = "AnyExpandableAdapter";

    private Context mContext;
    private List<ExpandedMenuModel> mListDataHeader; // header titles
//    private List<ExpandedChildModel> mListDataChild= new ArrayList<ExpandedChildModel>();

    // child data in format of header title, child title
    private HashMap<ExpandedMenuModel, List<ExpandedChildModel>> mapDataChild;
    AnimatedExpandableListView expandList;

    private LayoutInflater inflater = null;
    private GroupHolder groupHolder = null;
    private ChildHolder childHolder = null;

    public AnyExpandableAdapter(Context context, List<ExpandedMenuModel> listDataHeader) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
    }

    public AnyExpandableAdapter(Context context, List<ExpandedMenuModel> listDataHeader, HashMap<ExpandedMenuModel, List<ExpandedChildModel>> mapDataChild, AnimatedExpandableListView mView) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mapDataChild = mapDataChild;
        this.expandList = mView;
    }

    public void setDataList(List<ExpandedMenuModel> arrays){
        this.mListDataHeader = arrays;
    }

    public List<ExpandedMenuModel> getDataList(){
        return mListDataHeader;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
//        int i = mListDataHeader.size();
//        Log.d("GROUPCOUNT", String.valueOf(i));
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandedMenuModel headerItems = (ExpandedMenuModel) getGroup(groupPosition);

        View v = convertView;

        if (v == null) {
            groupHolder = new GroupHolder();
            v = inflater.inflate(R.layout.check_write_item01, parent, false);

            groupHolder.title = (TextView) v.findViewById(R.id.textView1);
            groupHolder.state = (TextView) v.findViewById(R.id.textView2);

            v.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder)v.getTag();
        }

        groupHolder.title.setTypeface(null, Typeface.BOLD);
        groupHolder.title.setText(headerItems.getTitle());

        String state;
        if(headerItems.getState().equals("1")){
            groupHolder.state.setBackgroundResource(R.drawable.box_green);
            state="양호";
        }else if(headerItems.getState().equals("2")){
            groupHolder.state.setBackgroundResource(R.drawable.box_red);
            state="이상";
        }else{
            groupHolder.state.setBackgroundResource(R.drawable.box_basic);
            state="미점검";
            groupHolder.state.setVisibility(View.GONE);
        }
        groupHolder.state.setText(state);

        return v;
    }

    @Override
    public ExpandedChildModel getChild(int groupPosition, int childPosition) {
//        Log.d("CHILD", mListDataChild.get(this.mListDataHeader.get(groupPosition)).get(childPosition).toString());
        return mListDataHeader.get(groupPosition).childDatas;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
//        return mListDataHeader.get(groupPosition).getChildLists().size();
        return 1;
//        return this.mapDataChild.get(this.mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ExpandedChildModel childItems = getChild(groupPosition, childPosition);
        final ExpandedMenuModel headerItems = (ExpandedMenuModel) getGroup(groupPosition);

        View v = convertView;

        if(v == null){
            childHolder = new ChildHolder();
            v = inflater.inflate(R.layout.check_write_item02, null);
            childHolder.etc = (EditText) v.findViewById(R.id.editText1);
            childHolder.state = (RadioGroup) v.findViewById(R.id.radioGroup);
            v.setTag(childHolder);

        }else{
            childHolder = (ChildHolder)v.getTag();

        }
        childHolder.etc.addTextChangedListener(new CheckWriteFragment.MyWatcher(childHolder.etc,childPosition));

        childHolder.etc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    childItems.setEtc(((EditText)v).getText().toString());
                }
            }
        });
        //라디오버튼 클릭 이벤트
        childHolder.state.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //UtilClass.logD("onCheckedChanged",checkedId+"");
                switch (checkedId){
                    case R.id.radio1:
                        headerItems.setState("1");
                        break;
                    case R.id.radio2:
                        headerItems.setState("2");
                        break;
                    default:
                        break;
                }
            }
        });

        childHolder.etc.setText(childItems.getEtc());

        if(headerItems.getState().equals("1")){
            childHolder.state.check(R.id.radio1);
        }else if(headerItems.getState().equals("2")){
            childHolder.state.check(R.id.radio2);
        }else{
            childHolder.state.check(R.id.radio2);
        }

        return v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

}


