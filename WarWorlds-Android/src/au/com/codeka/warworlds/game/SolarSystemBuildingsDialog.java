package au.com.codeka.warworlds.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import au.com.codeka.warworlds.R;
import au.com.codeka.warworlds.model.BuildingDesign;
import au.com.codeka.warworlds.model.BuildingDesignManager;
import au.com.codeka.warworlds.model.Colony;

public class SolarSystemBuildingsDialog extends Dialog {
    private SolarSystemActivity mActivity;

    public SolarSystemBuildingsDialog(SolarSystemActivity activity) {
        super(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.solarsystem_buildings);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = LayoutParams.MATCH_PARENT;
        params.width = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        final BuildingListAdapter adapter = new BuildingListAdapter();
        adapter.setDesigns(BuildingDesignManager.getInstance().getDesigns());

        // make sure we're aware of any changes to the designs
        BuildingDesignManager.getInstance().addDesignsChangedListener(new BuildingDesignManager.DesignsChangedListener() {
            @Override
            public void onDesignsChanged() {
                adapter.setDesigns(BuildingDesignManager.getInstance().getDesigns());
            }
        });

        ListView availableDesignsList = (ListView) findViewById(R.id.buildings_available);
        availableDesignsList.setAdapter(adapter);
        availableDesignsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                BuildingDesign design = (BuildingDesign) adapter.getItem(position);
                args.putString("au.com.codeka.warworlds.BuildingID", design.getID());
                mActivity.showDialog(SolarSystemActivity.BUILDINGS_CONFIRM_DIALOG, args);
            }
        });
    }

    public void setColony(Colony colony) {
        //mColony = colony;
    }

    /**
     * This adapter is used to populate the list of buildings in one of the views.
     */
    private class BuildingListAdapter extends BaseAdapter {
        private List<BuildingDesign> mDesigns;

        public void setDesigns(Map<String, BuildingDesign> designs) {
            mDesigns = new ArrayList<BuildingDesign>(designs.values());
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDesigns.size();
        }

        @Override
        public Object getItem(int position) {
            return mDesigns.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                view = (ViewGroup) inflater.inflate(R.layout.solarsystem_buildings_design, null);
            }

            ImageView icon = (ImageView) view.findViewById(R.id.building_icon);
            TextView row1 = (TextView) view.findViewById(R.id.building_row1);
            TextView row2 = (TextView) view.findViewById(R.id.building_row2);
            TextView row3 = (TextView) view.findViewById(R.id.building_row3);

            BuildingDesign design = mDesigns.get(position);

            Bitmap bm = BuildingDesignManager.getInstance().getDesignIcon(design);
            if (bm != null) {
                icon.setImageBitmap(bm);
            } else {
                icon.setImageBitmap(null);
            }

            row1.setText(design.getName());
            row2.setText(String.format("$ %d - %.2f hours", design.getBuildCost(),
                    (float) design.getBuildTimeSeconds() / 3600.0f));
            row3.setText("Required: none");

            return view;
        }
    }
}