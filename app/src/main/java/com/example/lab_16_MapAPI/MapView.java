package com.example.lab_16_MapAPI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.example.lab_16_MapAPI.model.Shape;
import com.example.lab_16_MapAPI.model.Tile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapView extends SurfaceView
{
    String base;

    public static DB database;

    Activity ctx;

    Canvas pubCanvas;

    ArrayList<Tile> masTile = new ArrayList<>();

    int width;
    int height;

    Paint paintCoastline;
    Paint paintRiver;
    Paint paintRailroad;
    Paint paintRoad;

    public int scale;

    int[] levels;
    int[] xtiles;
    int[] ytiles;
    float[] resolutions;

    int tilew = 100;
    int tileh = 100;

    public float ofs_x;
    public float ofs_y;

    float last_x = 0;
    float last_y = 0;

    float lat0, lon0;
    float lat1, lon1;

    boolean coastline;
    boolean river;
    boolean railroad;
    boolean road;

    public int lifetimeTile;

    ArrayList<ArrayList<Shape>> coastlines = new ArrayList<>();
    ArrayList<ArrayList<Shape>> rivers = new ArrayList<>();
    ArrayList<ArrayList<Shape>> railroads = new ArrayList<>();
    ArrayList<ArrayList<Shape>> roads = new ArrayList<>();

    public MapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        ctx = (Activity) context;

        database = new DB(ctx, "Map.db", null, 1);

        paintCoastline = new Paint();
        paintCoastline.setColor((Integer) database.GetSetting("colorCoastlines"));
        paintCoastline.setStyle(Paint.Style.STROKE);

        paintRiver = new Paint();
        paintRiver.setColor((Integer) database.GetSetting("colorRivers"));
        paintRiver.setStyle(Paint.Style.STROKE);

        paintRailroad = new Paint();
        paintRailroad.setColor((Integer) database.GetSetting("colorRailroads"));
        paintRailroad.setStyle(Paint.Style.STROKE);

        paintRoad = new Paint();
        paintRoad.setColor((Integer) database.GetSetting("colorRoads"));
        paintRoad.setStyle(Paint.Style.STROKE);

        base = (String) database.GetSetting("http");
        ofs_x = (float) database.GetSetting("ofsX");
        ofs_y = (float) database.GetSetting("ofsY");
        scale = (int) database.GetSetting("scale");
        lifetimeTile = (int) database.GetSetting("lifetimeTile");

        int[] checks = database.GetChecksShapes();

        coastline = checks[0] != 0;

        river = checks[1] != 0;

        railroad = checks[2] != 0;

        road = checks[3] != 0;

        APIHelper req = new APIHelper(ctx)
        {
            @Override
            public void on_ready(String res)
            {
                try {
                    JSONArray arr = new JSONArray(res);

                    levels = new int[arr.length()];
                    xtiles = new int[arr.length()];
                    ytiles = new int[arr.length()];
                    resolutions = new float[arr.length()];

                    for (int i = arr.length() - 1; i >= 0; i--)
                    {
                        JSONObject obj = arr.getJSONObject(i);

                        levels[arr.length() - i - 1] = Integer.parseInt(obj.getString("level"));
                        xtiles[arr.length() - i - 1] = Integer.parseInt(obj.getString("xtiles"));
                        ytiles[arr.length() - i - 1] = Integer.parseInt(obj.getString("ytiles"));
                        resolutions[arr.length() - i - 1] = Float.parseFloat(obj.getString("resolution"));
                    }

                    setWillNotDraw(false);
                    invalidate();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        req.send(base + "raster");

        Runnable cashTile = () ->
        {
            while (true)
            {
                database.CheckTile();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread threadCheckTile = new Thread(cashTile);
        threadCheckTile.start();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        pubCanvas = canvas;

        width = getWidth();
        height = getHeight();

        canvas.drawColor(Color.rgb(255,255,255));

        int screen_x0 = 0;
        int screen_y0 = 0;
        int screen_x1 = getWidth()-1;
        int screen_y1 = getHeight() -1;

        int xt = xtiles[scale];
        int yt = ytiles[scale];

        for (int y = 0; y < yt; y++)
        {
            for (int x = 0; x < xt; x++)
            {
                float x0 = x * tilew + ofs_x;
                float y0 = y * tileh + ofs_y;
                float x1 = x0 + tilew;
                float y1 = y0 + tileh;

                if (!Rect_rect(x0, y0, x1, y1, screen_x0, screen_y0, screen_x1, screen_y1))
                {
                    continue;
                }

                Tile t = GetTile(x, y, levels[scale]);

                if (t.img != null)
                {
                    canvas.drawBitmap(t.img, x0, y0, paintCoastline);
                }
            }
        }

        invalidate();

        if (coastline)
        {
            DrawShapes(coastlines, paintCoastline);
        }

        if (river)
        {
            DrawShapes(rivers, paintRiver);
        }

        if (railroad)
        {
            DrawShapes(railroads, paintRailroad);
        }

        if (road)
        {
            DrawShapes(roads, paintRoad);
        }
    }

    public void DrawShapes(ArrayList<ArrayList<Shape>> shapes, Paint p)
    {
        for (int i = 0; i < shapes.size(); i++)
        {
            ArrayList<Shape> shape = shapes.get(i);

            float px0 = Map(shape.get(0).x, lat0, lat1, 0, width);
            float py0 = Map(shape.get(0).y, lon0, lon1, 0, height);

            for (int pi = 1; pi < shape.size(); pi++)
            {
                float px1 = Map(shape.get(pi).x, lat0, lat1, 0, width);
                float py1 = Map(shape.get(pi).y, lon0, lon1, 0, height);

                pubCanvas.drawLine(px0, py0, px1, py1, p);

                px0 = px1;
                py0 = py1;
            }
        }

        invalidate();
    }

    public Tile GetTile(int x, int y, int level)
    {
        for (int i = 0; i < masTile.size(); i++)
        {
            Tile t = masTile.get(i);
            if (t.x == x && t.y == y && t.level == level)
            {
                return t;
            }
        }

        Tile dt = database.GetTile(x, y, level);

        if (dt != null)
        {
            masTile.add(dt);
            return dt;
        }

        Tile nt = new Tile(x, y, level, ctx, base, lifetimeTile);
        masTile.add(nt);
        return nt;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();

                ofs_x += x - last_x;
                ofs_y += y - last_y;

                invalidate();

                last_x = x;
                last_y = y;
                return true;

            case MotionEvent.ACTION_DOWN:
                coastlines.clear();
                rivers.clear();
                railroads.clear();
                roads.clear();

                invalidate();

                last_x = event.getX();
                last_y = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                Update_viewport();

                if (coastline)
                    Request("coastline", coastlines);
                if (river)
                    Request("river", rivers);
                if (railroad)
                    Request("railroad", railroads);
                if (road)
                    Request("road", roads);

                return true;
        }
        return false;
    }

    public void Request(String typeRequestShape, ArrayList<ArrayList<Shape>> newArrayList)
    {
        APIHelper req = new APIHelper(ctx)
        {
            @Override
            public void on_ready(String res)
            {
                try
                {
                    JSONArray allArray = new JSONArray(res);

                    for (int i = 0; i < allArray.length(); i++)
                    {
                        JSONArray curShape = allArray.getJSONArray(i);

                        ArrayList<Shape> shapeList = new ArrayList<>();

                        for (int j = 0; j < curShape.length(); j++)
                        {
                            JSONObject line = curShape.getJSONObject(j);

                            Shape newShape = new Shape((float) line.getDouble("x"), (float) line.getDouble("y"));
                            shapeList.add(newShape);
                        }

                        newArrayList.add(shapeList);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        req.send(base + typeRequestShape + "/" + levels[scale] + "?lat0=" + lat0 + "&lon0=" + lon0 + "&lat1=" + lat1 + "&lon1=" + lon1);
    }

    //Проверка на пересечение
    public boolean Rect_rect(double ax0, double ay0, double ax1, double ay1, double bx0, double by0, double bx1, double by1)
    {
        if (ax1 < bx0) return false;
        if (ax0 > bx1) return false;
        if (ay1 < by0) return false;
        if (ay0 > by1) return false;
        return true;
    }

    public void Update_viewport()
    {
        lat0 = -ofs_x * resolutions[scale] - 180.0f;
        lon0 = 90.0f + ofs_y * resolutions[scale];
        lat1 = lat0 + width * resolutions[scale];
        lon1 = lon0 - height * resolutions[scale];
    }

    public float Map(float coordinate, float x0, float x1, int a, int b)
    {
        float t = (coordinate - x0) / (x1 - x0);
        return a + (b - a) * t;
    }

    public void ZoomOut()
    {
        if (scale == 0) return;
        scale --;

        masTile.clear();
        coastlines.clear();
        rivers.clear();
        railroads.clear();
        roads.clear();

        ofs_x += width / 2.0f;
        ofs_y += height / 2.0f;

        ofs_x /= 2.0f;
        ofs_y /= 2.0f;

        invalidate();
    }

    public void ZoomIn()
    {
        if (scale > levels.length - 2) return;
        scale ++;

        masTile.clear();
        coastlines.clear();
        rivers.clear();
        railroads.clear();
        roads.clear();

        ofs_x *= 2;
        ofs_y *= 2;

        ofs_x -= width / 2.0f;
        ofs_y -= height / 2.0f;

        invalidate();
    }
}
