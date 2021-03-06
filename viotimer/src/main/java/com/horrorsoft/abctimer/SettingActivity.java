package com.horrorsoft.abctimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.horrorsoft.abctimer.adapters.SettingDialogAdapter;
import com.horrorsoft.abctimer.common.ApplicationData;
import com.horrorsoft.abctimer.data.ICommonData;
import com.horrorsoft.abctimer.data.NumericData;
import com.horrorsoft.abctimer.data.RadioButtonAndComboBoxData;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexey
 * Date: 24.10.13
 * Time: 23:16
 */

@Fullscreen
@EActivity(R.layout.settinglayout)
public class SettingActivity extends ActivityWithBluetoothStatuses implements AdapterView.OnItemClickListener, TextView.OnEditorActionListener, View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    final static int EDIT_SETTING_DIALOG = 1;
    final static int PLUS_OPERATION = 1;
    final static int MINUS_OPERATION = 2;

    @Bean
    protected ApplicationData commonData;

    @ViewById
    ListView listViewForSettingsDialog;

    @ViewById(R.id.imageViewBluetoothStatus)
    protected ImageView imageViewBluetoothStatus;

    @Bean
    ApplicationData applicationData;

    List<ICommonData> listOfData;
    ICommonData dataForDialog;
    boolean isValueConfirmed;
    int lastValue;
    int lastPosition;
    int lastComboBoxOrRadioButtonIndexSelected;
    View viewForSpinBoxDialog;
    int plusOrMinusMode;
    int autoPushCount = 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            boolean stopTimer = false;
            NumericData numericData = (NumericData) dataForDialog;
            if (numericData != null) {
                int currentValue = numericData.getCurrentValue();
                int step = numericData.getStep();
                int stepMultiplier = ++autoPushCount / 20 + 1;
                int newStep = step * stepMultiplier * stepMultiplier;
                int minValue = numericData.getMinValue();
                int maxValue = numericData.getMaxValue();
                int newValue = minValue;
                boolean ok = false;
                switch (plusOrMinusMode) {
                    case MINUS_OPERATION: {
                        newValue = currentValue - newStep;
                        if (newValue < minValue) {
                            newValue = minValue;
                            stopTimer = true;
                        }
                        ok = true;
                    }
                    break;
                    case PLUS_OPERATION: {
                        newValue = currentValue + newStep;
                        if (newValue > maxValue) {
                            newValue = maxValue;
                            stopTimer = true;
                        }
                        ok = true;
                    }
                    break;
                    default:
                        break;
                }
                if (ok) {
                    numericData.setCurrentValue(newValue);
                    if (viewForSpinBoxDialog != null) {
                        EditText editText = (EditText) viewForSpinBoxDialog.findViewById(R.id.editTextSpinBox);
                        editText.setText(intWithDividerAndPrecisionToString(newValue, numericData.getDivider(), numericData.getPrecision()));
                    }
                }
            }
            if (stopTimer)
                timerHandler.removeCallbacks(this);
            else
                timerHandler.postDelayed(this, 50);
        }
    };

    private DialogInterface.OnDismissListener myDismissListener = new DialogInterface.OnDismissListener() {
        @SuppressWarnings("deprecation")
        @Override
        public void onDismiss(DialogInterface dialog) {
            removeDialog(EDIT_SETTING_DIALOG);
            if (dataForDialog != null && dataForDialog.getType() == ICommonData.TYPE_NUMERIC && !isValueConfirmed) {
                NumericData nd = (NumericData) dataForDialog;
                nd.setCurrentValue(lastValue);
            }
        }
    };

    @AfterViews
    public void init() {
        fillData();
        SettingDialogAdapter settingDialogAdapter = new SettingDialogAdapter(this, listOfData);
        listViewForSettingsDialog.setAdapter(settingDialogAdapter);
        listViewForSettingsDialog.setOnItemClickListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == EDIT_SETTING_DIALOG) {
            if (dataForDialog == null)
                return null;
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(dataForDialog.getDescription());
            adb.setPositiveButton("Ok", myClickListener);
            switch (dataForDialog.getType()) {
                case ICommonData.TYPE_RADIOBUTTON: {
                    createRadioButtonDialog(adb);
                }
                break;
                case ICommonData.TYPE_COMBOBOX: {
                    createComboBoxDialog(adb);
                }
                break;
                case ICommonData.TYPE_NUMERIC: {
                    createNumericDialog(adb);
                }
                break;
                case ICommonData.TYPE_SEPARATOR:
                case ICommonData.TYPE_UNKNOWN:
                    break;
            }
            Dialog dlg = adb.create();
            dlg.setOnDismissListener(myDismissListener);
            return dlg;
        }
        return null;
    }

    private void createNumericDialog(AlertDialog.Builder adb) {
        // workaround for disable warning
        ViewGroup parent = (ViewGroup) findViewById(R.id.container);
        View view = getLayoutInflater().inflate(R.layout.editnumbers, parent, false);
        if (view != null) {
            Log.d(ApplicationData.LOG_TAG, "create numeritc");
            viewForSpinBoxDialog = view;
            EditText editText = (EditText) view.findViewById(R.id.editTextSpinBox);
            NumericData numericData = (NumericData) dataForDialog;
            int currentValue = numericData.getCurrentValue();
            int precision = numericData.getPrecision();
            int divider = numericData.getDivider();
            lastValue = currentValue;
            isValueConfirmed = false;
            editText.setText(intWithDividerAndPrecisionToString(currentValue, divider, precision));
            editText.setOnEditorActionListener(this);
            TextView textViewSuffix = (TextView) view.findViewById(R.id.textViewSpinboxSuffix);
            final TextView textViewPrefix = (TextView) view.findViewById(R.id.textViewSpinboxPrefix);
            textViewPrefix.setText(numericData.getPrefix());
            textViewSuffix.setText(numericData.getSuffix());
            Button plus = (Button) view.findViewById(R.id.buttonSpinBoxPlus);
            Button minus = (Button) view.findViewById(R.id.buttonSpinBoxMinus);
            plus.setLongClickable(true);
            plus.setOnClickListener(this);
            plus.setOnLongClickListener(this);
            plus.setOnTouchListener(this);
            minus.setLongClickable(true);
            minus.setOnClickListener(this);
            minus.setOnLongClickListener(this);
            minus.setOnTouchListener(this);
            adb.setView(view);
        }
    }

    private String intWithDividerAndPrecisionToString(int value, int divider, int precision) {
        return ApplicationData.doubleToString(value / 1.0 / divider, precision);
    }

    private void createComboBoxDialog(AlertDialog.Builder adb) {
        Spinner spinner = new Spinner(this);

        RadioButtonAndComboBoxData radioButtonData = (RadioButtonAndComboBoxData) dataForDialog;
        if (radioButtonData != null) {
            List<RadioButtonAndComboBoxData.ItemData> listOfItemData = radioButtonData.getListOfItemData();
            String[] listOfItemForComboBox = new String[listOfItemData.size()];
            for (int i = 0; i < listOfItemData.size(); ++i) {
                listOfItemForComboBox[i] = listOfItemData.get(i).description;
            }

            int currentIndex = radioButtonData.getCurrentIndex();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.sherlock_spinner_item, listOfItemForComboBox);
            adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(currentIndex);
            spinner.setOnItemSelectedListener(myItemClickListener);
            lastComboBoxOrRadioButtonIndexSelected = currentIndex;
            adb.setView(spinner);
        }
    }

    AdapterView.OnItemSelectedListener myItemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            lastComboBoxOrRadioButtonIndexSelected = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private void createRadioButtonDialog(AlertDialog.Builder adb) {
        RadioButtonAndComboBoxData radioButtonData = (RadioButtonAndComboBoxData) dataForDialog;
        if (radioButtonData != null) {
            List<RadioButtonAndComboBoxData.ItemData> listOfItemData = radioButtonData.getListOfItemData();
            String[] listOfItemForComboBox = new String[listOfItemData.size()];
            for (int i = 0; i < listOfItemData.size(); ++i) {
                listOfItemForComboBox[i] = listOfItemData.get(i).description;
            }

            int currentIndex = radioButtonData.getCurrentIndex();
            lastComboBoxOrRadioButtonIndexSelected = currentIndex;
            adb.setSingleChoiceItems(listOfItemForComboBox, currentIndex, myClickListener);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dataForDialog = listOfData.get(position);
        lastPosition = position;
        showDialog(EDIT_SETTING_DIALOG);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                timerHandler.removeCallbacks(timerRunnable);
                switch (dataForDialog.getType()) {
                    case ICommonData.TYPE_RADIOBUTTON: {
                        updateRadiobuttonAndComboboxData(lastComboBoxOrRadioButtonIndexSelected);
                    }
                    break;
                    case ICommonData.TYPE_COMBOBOX: {
                        updateRadiobuttonAndComboboxData(lastComboBoxOrRadioButtonIndexSelected);
                    }
                    break;
                    case ICommonData.TYPE_NUMERIC: {
                        isValueConfirmed = true;
                        updateCurrentItem();
                    }
                }
            } else {
                lastComboBoxOrRadioButtonIndexSelected = which;
            }
        }
    };

    private void updateRadiobuttonAndComboboxData(int selectedIndex) {
        RadioButtonAndComboBoxData radioButtonAndComboboxData = (RadioButtonAndComboBoxData) dataForDialog;
        if (radioButtonAndComboboxData != null) {
            radioButtonAndComboboxData.setCurrentValueByIndex(selectedIndex);
            updateCurrentItem();
        }
    }

    private void updateCurrentItem() {
        // поиск вьюхи по позиции. Подходит только для видимых элементов
        ListView lvs = (ListView) findViewById(R.id.listViewForSettingsDialog);
        View v = lvs.getChildAt(lastPosition - lvs.getFirstVisiblePosition());
        if (v != null) {
            TextView tv = (TextView) v.findViewById(R.id.textViewDataDescription);
            tv.setText(dataForDialog.getDataDescription());
        }
    }


    private void fillData() {
        listOfData = commonData.getGlobalSettingData();
        if (listOfData == null) {
            listOfData = new ArrayList<>();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (event == null) {
            NumericData numericData = (NumericData) dataForDialog;
            CharSequence charSequence = v.getText();
            if (charSequence != null) {
                double dValue = Double.parseDouble(charSequence.toString());
                int value = (int) (dValue * numericData.getDivider());
                if (value < numericData.getMinValue()) {
                    value = numericData.getMinValue();
                } else if (value > numericData.getMaxValue()) {
                    value = numericData.getMaxValue();
                }
                numericData.setCurrentValue(value);
                v.setText(intWithDividerAndPrecisionToString(value, numericData.getDivider(), numericData.getPrecision()));
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        NumericData numericData = (NumericData) dataForDialog;
        if (numericData == null) {
            return;
        }
        int currentValue = numericData.getCurrentValue();
        int step = numericData.getStep();
        int minValue = numericData.getMinValue();
        int maxValue = numericData.getMaxValue();
        int newValue;
        switch (v.getId()) {
            case R.id.buttonSpinBoxMinus: {
                newValue = currentValue - step;
                if (newValue < minValue)
                    newValue = minValue;
            }
            break;
            case R.id.buttonSpinBoxPlus: {
                newValue = currentValue + step;
                if (newValue > maxValue) {
                    newValue = maxValue;
                }
            }
            break;
            default:
                return;
        }
        numericData.setCurrentValue(newValue);
        if (viewForSpinBoxDialog != null) {
            EditText editText = (EditText) viewForSpinBoxDialog.findViewById(R.id.editTextSpinBox);
            editText.setText(intWithDividerAndPrecisionToString(newValue, numericData.getDivider(), numericData.getPrecision()));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.buttonSpinBoxMinus) {
            plusOrMinusMode = MINUS_OPERATION;
        } else if (v.getId() == R.id.buttonSpinBoxPlus) {
            plusOrMinusMode = PLUS_OPERATION;
        } else {
            return false;
        }
        autoPushCount = 0;
        // включаем автоускорение
        timerHandler.postDelayed(timerRunnable, 0);
        return false;
    }

    // Выключаем автонажатие кнопки
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        return false;
    }

    @Override
    protected ImageView getImageViewBluetoothStatus() {
        return imageViewBluetoothStatus;
    }

    @Override
    protected ApplicationData getCommonData() {
        return commonData;
    }
}