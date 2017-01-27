package shareroute.nazib.com.shareroute;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class DottedCircleView extends View {

    private int radius;
    private int color;
    private int width;
    private int height;
    private Paint p;
    private BitmapDrawable pin;
    private int pinXOffset;
    private int pinYOffset;
    private float DASH_INTERVAL = .5f;

    public DottedCircleView(Context context) {
        super(context);
        color = getResources().getColor(R.color.colorPrimary);
        radius = 50;
        setup();
    }

    public DottedCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        color = getResources().getColor(R.color.colorPrimary);
        radius = 50;
        setup();
    }

    public DottedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.d("NAZIB", "DottedCircleView constructor");

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DottedCircleView, 0, 0);
        color = a.getColor(R.styleable.DottedCircleView_circleColor, getResources().getColor(R.color.colorPrimary));
        radius = a.getInteger(R.styleable.DottedCircleView_radius, 0);
        a.recycle();

        setup();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        if (radius == 0) {
            radius = Math.min(width, height) / 2 - (int) p.getStrokeWidth();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawCircle(width / 2, height / 2, radius, p);
        //draw the map marker in middle of the circle

        canvas.drawBitmap(pin.getBitmap(), (width / 2) - pinXOffset, (height / 2) - pinYOffset, null);
        invalidate();
    }

    private void setup() {
        p = new Paint();
        p.setColor(getResources().getColor(R.color.colorPrimary));
        p.setStrokeWidth(
                getResources().getDimension(R.dimen.activity_vertical_margin));
        DashPathEffect dashPath = new DashPathEffect(new float[]{DASH_INTERVAL,
                DASH_INTERVAL}, (float) 1.0);
        p.setPathEffect(dashPath);
        p.setStyle(Paint.Style.STROKE);

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_plus);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        pin = new BitmapDrawable(getResources(), smallMarker);

        //pin = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_plus_16px);
        pinXOffset = pin.getIntrinsicWidth() / 2;
        pinYOffset = pin.getIntrinsicHeight() / 2;
    }
}