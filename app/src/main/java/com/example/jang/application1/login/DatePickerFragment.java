package com.example.jang.application1.login;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();      // 오늘 날짜로 디폴트값을 설정하기 위해 캘린더 객체 선언
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);              // MONTH : 0~11
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day); // this는 리스너를 가르키는데 이 프래그먼트 클래스 자신을 가리킨다.
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        SignupPage activity = (SignupPage) getActivity();              // MainActivity의 birthday 버튼에 접근하기 위해 액티비티 객체 선언
        activity.birthday.setText(year+"-"+(month+1)+"-"+ day); // 유저가 선택한 날짜로 버튼 텍스트 변경
    }
}