package com.example.myapplication.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.usecase.GetAnalyticsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val period: Period = Period.Month,
    val summary: AnalyticsSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIndex: Int? = null
)

sealed interface AnalyticsEvent {
    data class PeriodSelected(val period: Period) : AnalyticsEvent
    data class BarSelected(val index: Int) : AnalyticsEvent
    object Retry : AnalyticsEvent
}

class AnalyticsViewModel(
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadAnalytics(Period.Month)
    }

    fun onEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.PeriodSelected -> {
                if (_uiState.value.period != event.period || _uiState.value.summary == null) {
                    _uiState.update { it.copy(period = event.period) }
                    loadAnalytics(event.period)
                }
            }

            is AnalyticsEvent.BarSelected -> {
                val summary = _uiState.value.summary
                if (summary != null && event.index in summary.points.indices) {
                    _uiState.update { it.copy(selectedIndex = event.index) }
                }
            }

            AnalyticsEvent.Retry -> {
                loadAnalytics(_uiState.value.period)
            }
        }
    }

    private fun loadAnalytics(period: Period) {
        fetchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchJob = viewModelScope.launch {
            getAnalyticsUseCase(period)
                .catch { cause ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = cause.message ?: "Failed to load analytics"
                        )
                    }
                }
                .collect { summary ->
                    _uiState.update {
                        it.copy(
                            summary = summary,
                            isLoading = false,
                            error = null,
                            selectedIndex = summary.highlightIndex.takeIf { idx ->
                                idx >= 0 && idx < summary.points.size
                            }
                        )
                    }
                }
        }
    }
}
