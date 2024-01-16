package com.example.amsems;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.example.amsems.utils.ALoadingDialog;
import com.example.amsems.utils.DisplayActivityDataAsyncTask;
import com.example.amsems.utils.DisplayEventDataAsyncTask;
import com.example.amsems.utils.DrawableUtils;
import com.example.amsems.utils.EventRecyclerViewInterface;
import com.example.amsems.utils.FetchActivityDataAsyncTask;
import com.example.amsems.utils.FetchEventDataAsyncTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment implements EventRecyclerViewInterface {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    List<CalendarDay> events;
    CalendarView calendarView;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ArrayList<String> _name, _date, _color, _id;
    ArrayList<String> _nameA, _dateA, _colorA, _idA;
    Date selectedDate;
    private ALoadingDialog aLoadingDialog;

    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        aLoadingDialog = new ALoadingDialog(getActivity());
        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.rcview);
        recyclerView2 = view.findViewById(R.id.rcview2);

        events = new ArrayList<>();

        _name = new ArrayList<>();
        _date = new ArrayList<>();
        _color = new ArrayList<>();
        _id = new ArrayList<>();

        _nameA = new ArrayList<>();
        _dateA = new ArrayList<>();
        _colorA = new ArrayList<>();
        _idA = new ArrayList<>();

        aLoadingDialog.show();
        new GetEventDatesAsyncTask().execute();

        Date now = new Date();
        // Define the date format
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");


        String formattedDateCurrent = sdf2.format(now);
        new FetchEventDataAsyncTask(getContext(), recyclerView, _id, _name, _date, _color, this).execute(formattedDateCurrent);
        new FetchActivityDataAsyncTask(getContext(), recyclerView2, _idA, _nameA, _dateA, _colorA, this).execute(formattedDateCurrent);
        //Toast.makeText(getActivity(), formattedDateCurrent, Toast.LENGTH_SHORT).show();

        calendarView.setOnDayClickListener(eventDay -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            selectedDate = eventDay.getCalendar().getTime();
            String formattedDate = sdf.format(selectedDate);
            new DisplayActivityDataAsyncTask(getContext(), recyclerView2, _idA,_nameA, _dateA, _colorA, this).execute(formattedDate);
            new DisplayEventDataAsyncTask(getContext(), recyclerView, _id,_name, _date, _color, this).execute(formattedDate);
            //Toast.makeText(getActivity(), formattedDate, Toast.LENGTH_SHORT).show();
        });
        return view;
    }
    public static List<Date> getDatesBetween(String startDateString, String endDateString) throws ParseException {
        List<Date> dates = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse(startDateString);
        Date endDate = dateFormat.parse(endDateString);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate) || calendar.getTime().equals(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    @Override
    public void onEventClick(int position, String eventid) {
        Intent intent = new Intent(getActivity(), EventInfoActivity.class);
        intent.putExtra("EventID", eventid);
        startActivity(intent);
    }
    @Override
    public void onActivityClick(int position, String actid) {
        Intent intent = new Intent(getActivity(), ActivityInfo_Activity.class);
        intent.putExtra("ActivityID", actid);
        startActivity(intent);
    }

    private class GetEventDatesAsyncTask extends AsyncTask<Void, Void, List<CalendarDay>> {
        @Override
        protected List<CalendarDay> doInBackground(Void... voids) {
            List<CalendarDay> events = new ArrayList<>();

            try {
                Connection connection = SQL_Connection.connectionClass();

                if (connection != null) {
                    String query = "SELECT Start_Date, End_Date FROM tbl_events";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                         ResultSet resultSet = preparedStatement.executeQuery()) {

                        if (resultSet != null) {
                            while (resultSet.next()) {
                                String startDateString = resultSet.getString("Start_Date");
                                String endDateString = resultSet.getString("End_Date");

                                List<Date> datesBetween = getDatesBetween(startDateString, endDateString);
                                Calendar calendarnow = Calendar.getInstance();
                                CalendarDay calendarDaynow = new CalendarDay(calendarnow);
                                calendarDaynow.setBackgroundDrawable(DrawableUtils.getDayCircle(getActivity(), com.applandeo.materialcalendarview.R.color.defaultColor, android.R.color.transparent));
                                events.add(calendarDaynow);

                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date startDate = dateFormat.parse(startDateString);
                                Date endDate = dateFormat.parse(endDateString);

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(startDate);

                                CalendarDay calendarDay = new CalendarDay(calendar);
                                calendarDay.setBackgroundDrawable(DrawableUtils.getCircleDrawableWithText(getContext(), ""));
                                calendarDay.setLabelColor(R.color.white);
                                events.add(calendarDay);

                                Calendar calendar2 = Calendar.getInstance();
                                calendar2.setTime(endDate);

                                CalendarDay calendarDay2 = new CalendarDay(calendar2);
                                calendarDay2.setBackgroundDrawable(DrawableUtils.getCircleDrawableWithText(getContext(), ""));
                                calendarDay2.setLabelColor(R.color.white);
                                events.add(calendarDay2);

                                for (Date date : datesBetween) {
                                    Calendar calendar3 = Calendar.getInstance();
                                    calendar3.setTime(date);
                                    CalendarDay calendarDay3 = new CalendarDay(calendar3);
                                    calendarDay3.setBackgroundDrawable(DrawableUtils.getCircleDrawableWithText(getContext(), ""));
                                    calendarDay3.setLabelColor(R.color.white);
                                    events.add(calendarDay3);
                                }
                            }
                        }
                    }
                    query = "SELECT Date FROM tbl_activities";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                         ResultSet resultSet = preparedStatement.executeQuery()) {

                        if (resultSet != null) {
                            while (resultSet.next()) {
                                String date = resultSet.getString("Date");

                                Calendar calendarnow = Calendar.getInstance();
                                CalendarDay calendarDaynow = new CalendarDay(calendarnow);
                                calendarDaynow.setBackgroundDrawable(DrawableUtils.getDayCircle(getActivity(), com.applandeo.materialcalendarview.R.color.defaultColor, android.R.color.transparent));
                                events.add(calendarDaynow);

                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date startDate = dateFormat.parse(date);

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(startDate);

                                CalendarDay calendarDay = new CalendarDay(calendar);
                                calendarDay.setBackgroundDrawable(DrawableUtils.getCircleDrawableWithText(getContext(), ""));
                                calendarDay.setLabelColor(R.color.white);
                                events.add(calendarDay);

                            }
                        }
                    } finally {
                        connection.close();
                    }
                }
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return events;
        }

        @Override
        protected void onPostExecute(List<CalendarDay> result) {
            if (result != null && !result.isEmpty()) {
                calendarView.setCalendarDays(result);
            }
            aLoadingDialog.cancel();
        }
    }
}