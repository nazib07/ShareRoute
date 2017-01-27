package shareroute.nazib.com.shareroute;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.SeekBar;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by nazib.ullah on 1/27/2017.
 */
public class SliderDialog extends Dialog implements DiscreteSeekBar.OnProgressChangeListener {

    Context context;

    public SliderDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.slider_dialog);

        context = super.getContext();

        int sliderValueFromPreference = CommonUtils.getTunerValueFromPreference(context);
        Log.d("NAZIB", "" + sliderValueFromPreference);

        DiscreteSeekBar seekBar = (DiscreteSeekBar) findViewById(R.id.distance_tuner);
        seekBar.setProgress(sliderValueFromPreference);

        seekBar.setOnProgressChangeListener(this);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        Log.d("SHARE_ROUTE", "onProgressChanged: " + value);
        CommonUtils.setTunerValueInPreference(context, value);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
