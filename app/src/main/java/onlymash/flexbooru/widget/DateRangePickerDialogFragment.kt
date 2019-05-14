/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.widget

import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TabHost
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import onlymash.flexbooru.R

class DateRangePickerDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker

    private lateinit var buttonCancel: AppCompatButton
    private lateinit var buttonOk: AppCompatButton

    private var dateRangeSetListener: OnDateRangeSetListener? = null

    private var startDay = 0
    private var startMonth = 0
    private var startYear = 0
    private var endDay = 0
    private var endMonth = 0
    private var endYear = 0

    private var minDate = 0L
    private var maxDate = 0L

    private val startDateChangedListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        startDay = dayOfMonth
        startMonth = monthOfYear
        startYear = year
    }

    private val endDateChangedListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        endDay = dayOfMonth
        endMonth = monthOfYear
        endYear = year
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            startDay = getInt(START_DAY)
            startMonth = getInt(START_MONTH)
            startYear = getInt(START_YEAR)
            endDay = getInt(END_DAY)
            endMonth = getInt(END_MONTH)
            endYear = getInt(END_YEAR)
            minDate = getLong(MIN_DATE)
            maxDate = getLong(MAX_DATE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.widget_date_range_picker, container, false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val tabHost: TabHost = view.findViewById(R.id.tab_host)
        tabHost.setup()
        val startDatePage = tabHost.newTabSpec("start")
        startDatePage.setContent(R.id.start_date_group)
        startDatePage.setIndicator(getString(R.string.tab_start_date))
        val endDatePage = tabHost.newTabSpec("end")
        endDatePage.setContent(R.id.end_date_group)
        endDatePage.setIndicator(getString(R.string.tab_end_date))
        tabHost.addTab(startDatePage)
        tabHost.addTab(endDatePage)
        startDatePicker = view.findViewById(R.id.start_date_picker)
        startDatePicker.init(startYear, startMonth, startDay, startDateChangedListener)
        endDatePicker = view.findViewById(R.id.end_date_picker)
        endDatePicker.init(endYear, endMonth, endDay, endDateChangedListener)
        startDatePicker.minDate = minDate
        startDatePicker.maxDate = maxDate
        endDatePicker.minDate = minDate
        endDatePicker.maxDate = maxDate
        buttonCancel = view.findViewById(R.id.cancel)
        buttonOk = view.findViewById(R.id.ok)
        buttonCancel.setOnClickListener(this)
        buttonOk.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View?) {
        if (v == buttonOk) {
            dateRangeSetListener?.onDateRangeSet(
                startDay = startDatePicker.dayOfMonth,
                startMonth = startDatePicker.month,
                startYear = startDatePicker.year,
                endDay = endDatePicker.dayOfMonth,
                endMonth = endDatePicker.month,
                endYear = endDatePicker.year
            )
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(R.drawable.background_dialog_picker)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(START_YEAR, startDatePicker.year)
            putInt(START_MONTH, startDatePicker.month)
            putInt(START_DAY, startDatePicker.dayOfMonth)
            putInt(END_YEAR, endDatePicker.year)
            putInt(END_MONTH, endDatePicker.month)
            putInt(END_DAY, endDatePicker.dayOfMonth)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            startDatePicker.init(getInt(START_YEAR), getInt(START_MONTH), getInt(START_DAY), startDateChangedListener)
            endDatePicker.init(getInt(END_YEAR), getInt(END_MONTH), getInt(END_DAY), endDateChangedListener)
        }
    }

    fun setOnDateRangeSetListener(listener: OnDateRangeSetListener?) {
        dateRangeSetListener = listener
    }

    interface OnDateRangeSetListener {
        fun onDateRangeSet(
            startDay: Int,
            startMonth: Int,
            startYear: Int,
            endDay: Int,
            endMonth: Int,
            endYear: Int
        )
    }

    companion object {
        private const val START_YEAR = "start_year"
        private const val START_MONTH = "start_month"
        private const val START_DAY = "start_day"

        private const val END_YEAR = "end_year"
        private const val END_MONTH = "end_month"
        private const val END_DAY = "end_day"

        private const val MAX_DATE = "max_date"
        private const val MIN_DATE = "min_date"

        fun newInstance(
            listener: OnDateRangeSetListener? = null,
            startDay: Int,
            startMonth: Int,
            startYear: Int,
            endDay: Int,
            endMonth: Int,
            endYear: Int,
            minDate: Long,
            maxDate: Long
        ): DateRangePickerDialogFragment {
            return DateRangePickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(START_YEAR, startYear)
                    putInt(START_MONTH, startMonth)
                    putInt(START_DAY, startDay)
                    putInt(END_YEAR, endYear)
                    putInt(END_MONTH, endMonth)
                    putInt(END_DAY, endDay)
                    putLong(MIN_DATE, minDate)
                    putLong(MAX_DATE, maxDate)
                }
                setOnDateRangeSetListener(listener)
            }
        }
    }
}