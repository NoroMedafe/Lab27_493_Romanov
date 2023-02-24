package com.example.lab_16_MapAPI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Activity_Map extends AppCompatActivity {

    MapView mv;

    static DB database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mv = findViewById(R.id.mapView);

        database = mv.database;

        mv.invalidate();
    }

    @Override
    protected void onStop()
    {
        database.UpdateFieldInSetting("ofsX", mv.ofs_x);
        database.UpdateFieldInSetting("ofsY", mv.ofs_y);
        database.UpdateFieldInSetting("scale", mv.scale);

        int[] checks = new int[4];

        if (mv.coastline)
            checks[0] = 1;
        else
            checks[0] = 0;

        if (mv.river)
            checks[1] = 1;
        else
            checks[1] = 0;

        if (mv.railroad)
            checks[2] = 1;
        else
            checks[2] = 0;

        if (mv.road)
            checks[3] = 1;
        else
            checks[3] = 0;

        database.UpdateCheckShapes(checks);

        super.onStop();
    }

    public void ZoomOut(View v)
    {
        mv.ZoomOut();
    }

    public void ZoomIn(View v)
    {
        mv.ZoomIn();
    }

    public void OpenSetting(View v)
    {
        Intent i = new Intent(this, Activity_Setting.class);

        i.putExtra("coastline", mv.coastline);
        i.putExtra("river", mv.river);
        i.putExtra("railroad", mv.railroad);
        i.putExtra("road", mv.road);
        i.putExtra("lifetimeTile", mv.lifetimeTile);

        int[] colorsShapes = new int[4];

        colorsShapes[0] = mv.paintCoastline.getColor();
        colorsShapes[1] = mv.paintRiver.getColor();
        colorsShapes[2] = mv.paintRailroad.getColor();
        colorsShapes[3] = mv.paintRoad.getColor();

        i.putExtra("colors", colorsShapes);

        Activity_Setting.database = database;

        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            mv.coastline = data.getBooleanExtra("coastline", mv.coastline);
            mv.river = data.getBooleanExtra("river", mv.river);
            mv.railroad = data.getBooleanExtra("railroad", mv.railroad);
            mv.road = data.getBooleanExtra("road", mv.road);
            mv.lifetimeTile = data.getIntExtra("lifetimeTile", 1000);

            int[] colors = data.getIntArrayExtra("colors");

            mv.paintCoastline.setColor(colors[0]);
            mv.paintRiver.setColor(colors[1]);
            mv.paintRailroad.setColor(colors[2]);
            mv.paintRoad.setColor(colors[3]);
        }
    }
}