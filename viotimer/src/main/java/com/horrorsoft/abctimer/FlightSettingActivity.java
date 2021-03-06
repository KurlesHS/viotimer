package com.horrorsoft.abctimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.horrorsoft.abctimer.adapters.AlgorithmAdapter;
import com.horrorsoft.abctimer.common.ApplicationData;
import com.horrorsoft.abctimer.data.AlgorithmData;
import com.horrorsoft.abctimer.data.AlgorithmHandler;
import com.horrorsoft.abctimer.data.AlgorithmRowData;
import com.horrorsoft.abctimer.dialogs.EditAlgorithmDataDialog;
import com.horrorsoft.abctimer.dialogs.EditAlgorithmDataDialog_;
import com.horrorsoft.abctimer.dialogs.IDialogFragmentClickListener;
import com.horrorsoft.abctimer.dialogs.SelectItemPositionForAlgorithmTableDialog;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 28.10.13
 * Time: 20:57
 */
@Fullscreen
@EActivity(R.layout.activity_fligth_setting)
public class FlightSettingActivity extends FragmentActivityWithBluetoothStatuses implements View.OnClickListener, IDialogFragmentClickListener {

    @InstanceState
    protected int currentAlgorithmNumber;
    @InstanceState
    protected int currentServoNumber;

    @ViewById(R.id.imageViewBluetoothStatus)
    protected ImageView imageViewBluetoothStatus;


    @Bean
    protected ApplicationData applicationData;
    @Bean
    AlgorithmHandler algorithmHandler;
    @Bean
    protected AlgorithmAdapter algorithmAdapter;

    @ViewById(R.id.listViewForAlgorithm)
    ListView algorithmListView;

    @ViewById(R.id.scrollViewForAlgorithmButtons)
    ScrollView scrollViewForAlgorithmButtons;

    @ViewById(R.id.linearLayoutForServoButtons)
    LinearLayout linearLayoutForServoButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        algorithmListView.setScrollbarFadingEnabled(false);
        scrollViewForAlgorithmButtons.setScrollbarFadingEnabled(false);
        FillAlgorithmButton(applicationData.getAlgorithmData());
        algorithmAdapter.setAlgorithmHandler(algorithmHandler);
        algorithmListView.setAdapter(algorithmAdapter);
        int servoNum = 0;
        for (int r = 0; r < linearLayoutForServoButtons.getChildCount(); ++r) {
            View v = linearLayoutForServoButtons.getChildAt(r);
            if (v instanceof ImageButton) {
                ImageButton button = (ImageButton) linearLayoutForServoButtons.getChildAt(r);
                if (button != null) {
                    if (servoNum == currentServoNumber) {
                        button.setSelected(true);
                    }
                    button.setTag(R.id.ServoNumberButton, servoNum++);
                    button.setId(R.id.ServoNumberButtonId);
                    button.setOnClickListener(this);
                }
            }
        }
        algorithmHandler.setCurrentAlgorithmAndServoNum(currentAlgorithmNumber, currentServoNumber);
        //FillTestAlgorithmData();
    }

    @Click(R.id.buttonAdd)
    protected void onAddButtonClicked() {
        SelectItemPositionForAlgorithmTableDialog dlg = new SelectItemPositionForAlgorithmTableDialog();
        dlg.show(getSupportFragmentManager(), "selitemdlg");
    }

    @Click(R.id.buttonChange)
    protected void onChangeButtonClicked() {
        AlgorithmRowData algorithmRowData = algorithmHandler.getAlgorithmRowData(algorithmHandler.getSelectedRow());
        if (algorithmRowData != null) {
            onItemLongClick1(algorithmRowData);
        }
    }

    @Click(R.id.buttonDelete)
    protected void onDeleteButtonPushed() {
        int selectedRow = algorithmHandler.getSelectedRow();
        if (selectedRow >= 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Really?");
            alertDialogBuilder.setMessage("Are you sure to delete selected row?");
            //null should be your on click listener
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    algorithmHandler.removeRow(algorithmHandler.getSelectedRow());
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", null);
            alertDialogBuilder.create().show();
        }
    }

    private void FillAlgorithmButton(AlgorithmData algorithmData) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutForChangeAlgorithmStep);
        for (int i = 0; i < algorithmData.getAlgorithmCount(); ++i) {
            String description = algorithmData.getAlgorithmDescription(i);
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.selected_button);
            button.setText(description);
            button.setTag(R.id.AlgorithmNumberButton, i);
            button.setId(R.id.AlgorithmNumberButtonId);
            button.setOnClickListener(this);
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, getResources().getInteger(R.integer.HeightChangeAlgorithmButtonInMm),
                    getResources().getDisplayMetrics());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) px);
            button.setLayoutParams(layoutParams);
            if (i == currentAlgorithmNumber) {
                button.setSelected(true);
            }
            linearLayout.addView(button);
        }
    }

    private void unCheckAlgorithmButton(LinearLayout linearLayout) {
        for (int r = 0; r < linearLayout.getChildCount(); ++r) {
            View v = linearLayout.getChildAt(r);
            if (v != null && v.isSelected()) {
                v.setSelected(false);
            }
        }

    }

    @ItemClick(R.id.listViewForAlgorithm)
    void personListItemClicked(AlgorithmRowData data) {
        //makeText(this, data.getPosition() + " " + data.getPosition() + " " + data.getServoPos(), LENGTH_SHORT).show();
        algorithmHandler.setSelectedRow(data.getPosition() - 1);
    }

    @ItemLongClick(R.id.listViewForAlgorithm)
    public boolean onItemLongClick1(AlgorithmRowData data) {
        int position = data.getPosition() - 1;
        algorithmHandler.setSelectedRow(position);
        AlgorithmHandler.InfoAboutRow infoAboutRow = algorithmHandler.getInfoAboutRow(position);
        if (infoAboutRow != null) {
            EditAlgorithmDataDialog dlg = new EditAlgorithmDataDialog_();
            int maxDelay = infoAboutRow.maxDelay;
            int minDelay = infoAboutRow.minDelay;
            int delay = infoAboutRow.delay;
            int servoPos = infoAboutRow.servoPos;

            Bundle argBundle = new Bundle();
            argBundle.putInt(EditAlgorithmDataDialog.keyMaxDelay, maxDelay);
            argBundle.putInt(EditAlgorithmDataDialog.keyMinDelay, minDelay);
            argBundle.putInt(EditAlgorithmDataDialog.keyDelay, delay);
            argBundle.putInt(EditAlgorithmDataDialog.keyServoPos, servoPos);
            argBundle.putInt(EditAlgorithmDataDialog.keyPosition, position);
            argBundle.putInt(EditAlgorithmDataDialog.keyServoNumber, currentServoNumber + 1);
            dlg.setArguments(argBundle);
            dlg.show(getSupportFragmentManager(), "editAlgDtaDlg");
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.AlgorithmNumberButtonId: {
                Button button = (Button) v;
                Integer i = (Integer) v.getTag(R.id.AlgorithmNumberButton);
                if (i != null) {
                    int algorithmNumber = i;
                    if (algorithmNumber != currentAlgorithmNumber) {
                        unCheckAlgorithmButton((LinearLayout) findViewById(R.id.layoutForChangeAlgorithmStep));
                        button.setSelected(true);
                        Log.d("MyTag", "ALGORITHM_NUMBER_BUTTON_ID: " + algorithmNumber);
                        currentAlgorithmNumber = algorithmNumber;
                        algorithmHandler.setCurrentAlgorithmNum(algorithmNumber);
                    }
                }
            }
            break;
            case R.id.ServoNumberButtonId: {
                ImageButton button = (ImageButton) v;
                Integer i = (Integer) v.getTag(R.id.ServoNumberButton);
                if (i != null) {
                    int servoNumber = i;
                    if (servoNumber != currentServoNumber) {
                        unCheckAlgorithmButton(linearLayoutForServoButtons);
                        button.setSelected(true);
                        Log.d("MyTag", "SERVO_NUMBER_BUTTON_ID: " + servoNumber);
                        currentServoNumber = servoNumber;
                        algorithmHandler.setCurrentServoNum(servoNumber);
                    }
                }
            }
            break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        algorithmAdapter.setAlgorithmHandler(null);
    }

    @Override
    public void onClick(int buttonId, Bundle bundle) {
        Log.d("MyTag", "button pressed: " + buttonId);
        switch (buttonId) {
            case R.id.InsertAlgorithmDataBottomCurrentItem: {
                // обработать вставку ряда таблицы ниже текущего элемента
                insertAlgorithmDataBottomCurrentItem();
            }
            break;
            case R.id.InsertAlgorithmDataUpperCurrentItem: {
                // обработать вставку ряда таблицы выше текущего элемента
                insertAlgorithmDataUpperCurrentItem();
            }
            break;
            case R.id.AlgorithmDataChanged: {
                if (bundle != null) {
                    int servoPos = bundle.getInt(EditAlgorithmDataDialog.keyServoPos);
                    int delay = bundle.getInt(EditAlgorithmDataDialog.keyDelay);
                    int position = bundle.getInt(EditAlgorithmDataDialog.keyPosition);
                    algorithmHandler.updateAlgorithmData(algorithmHandler.getSelectedRow(), position + 1, delay, servoPos);
                }
            }
            break;
            default:
                break;
        }
    }

    private void insertAlgorithmDataBottomCurrentItem() {
        int currentPos = algorithmHandler.getSelectedRow();
        if (currentPos < 0)
            currentPos = 0;
        ++currentPos;
        if (currentPos > algorithmHandler.getSize()) {
            currentPos = algorithmHandler.getSize();
        }
        AlgorithmHandler.InfoAboutRow infoAboutRow = algorithmHandler.getInfoInsertingAboutRow(currentPos);
        if (infoAboutRow != null) {
            algorithmHandler.insertRow(currentPos, infoAboutRow.delay, infoAboutRow.servoPos, false);
            algorithmHandler.setSelectedRow(currentPos);
        }
    }

    private void insertAlgorithmDataUpperCurrentItem() {
        int currentPos = algorithmHandler.getSelectedRow();
        if (currentPos < 0)
            currentPos = 0;
        AlgorithmHandler.InfoAboutRow infoAboutRow = algorithmHandler.getInfoInsertingAboutRow(currentPos);
        if (infoAboutRow != null) {
            algorithmHandler.insertRow(currentPos, infoAboutRow.delay, infoAboutRow.servoPos);
        }
    }

    @Override
    protected ImageView getImageViewBluetoothStatus() {
        return imageViewBluetoothStatus;
    }

    @Override
    protected ApplicationData getCommonData() {
        return applicationData;
    }
}