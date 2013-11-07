package com.horrorsoft.viotimer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.horrorsoft.viotimer.R;
import com.horrorsoft.viotimer.common.ApplicationData;

/**
 * Created with IntelliJ IDEA.
 * User: Alexey
 * Date: 07.11.13
 * Time: 0:19
 */
@EViewGroup(R.layout.algoritm_data_row_layout)
public class AlgorithmDataRowView extends LinearLayout {

    @ViewById(R.id.positionTextView)
    TextView positionTextView;
    @ViewById(R.id.delayTextView)
    TextView delayTextView;
    @ViewById(R.id.servoPosTextView)
    TextView servoPosTextView;

    public AlgorithmDataRowView(Context context) {
        super(context);
    }

    public AlgorithmDataRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlgorithmDataRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bind(int position, int delay, int servoPos) {
        positionTextView.setText(ApplicationData.doubleToString((double)position, 0, 2));
        delayTextView.setText(ApplicationData.getDelayText(delay));
        servoPosTextView.setText(ApplicationData.getServoPosString(servoPos));
    }
}

