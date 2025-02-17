package com.dicoding.finego.features.monthlyreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.finego.helper.Repository
import com.dicoding.finego.helper.Result
import kotlinx.coroutines.launch

class MonthlyReportViewModel(private val repository: Repository) : ViewModel() {

    private val _monthlyReport = MutableLiveData<Result<MonthlyReportResponse>>()
    val monthlyReport: LiveData<Result<MonthlyReportResponse>> get() = _monthlyReport

    fun fetchMonthlyReport(userId: String) {
        _monthlyReport.value = Result.Loading
        viewModelScope.launch {
            _monthlyReport.value = repository.getMonthlyReport(userId)
        }
    }
}