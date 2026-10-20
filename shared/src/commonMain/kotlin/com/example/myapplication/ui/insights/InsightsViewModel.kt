package com.example.myapplication.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.model.Subscription
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.domain.usecase.CancelSubscriptionUseCase
import com.example.myapplication.domain.usecase.DismissInsightUseCase
import com.example.myapplication.domain.usecase.GetInsightsUseCase
import com.example.myapplication.domain.usecase.GetSubscriptionsUseCase
import com.example.myapplication.domain.usecase.RestoreInsightUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val insights: List<Insight> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val monthlySubscriptionTotalPaise: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reviewTarget: Subscription? = null,
    val lastDismissed: Insight? = null
)

sealed interface InsightsEvent {
    data class DismissClicked(val id: String) : InsightsEvent
    object UndoDismiss : InsightsEvent
    data class ReviewClicked(val insightId: String) : InsightsEvent
    object ReviewClosed : InsightsEvent
    data class MarkCancelled(val subscriptionId: String) : InsightsEvent
    object Retry : InsightsEvent
}

class InsightsViewModel(
    private val getInsightsUseCase: GetInsightsUseCase,
    private val dismissInsightUseCase: DismissInsightUseCase,
    private val restoreInsightUseCase: RestoreInsightUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val cancelSubscriptionUseCase: CancelSubscriptionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        fetchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetchJob = viewModelScope.launch {
            combine(
                getInsightsUseCase(),
                getSubscriptionsUseCase()
            ) { insights, subscriptions ->
                Pair(insights, subscriptions)
            }.catch { cause ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = cause.message ?: "Failed to load insights"
                    )
                }
            }.collect { (insights, subscriptions) ->
                val monthlyTotal = subscriptions
                    .filter { it.status == SubscriptionStatus.Active }
                    .sumOf { it.amountPaise }

                _uiState.update {
                    it.copy(
                        insights = insights,
                        subscriptions = subscriptions,
                        monthlySubscriptionTotalPaise = monthlyTotal,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun onEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.DismissClicked -> {
                val insightToDismiss = _uiState.value.insights.find { it.id == event.id }
                if (insightToDismiss != null) {
                    viewModelScope.launch {
                        dismissInsightUseCase(event.id)
                        _uiState.update {
                            it.copy(
                                insights = it.insights.filterNot { insight -> insight.id == event.id },
                                lastDismissed = insightToDismiss
                            )
                        }
                    }
                }
            }

            InsightsEvent.UndoDismiss -> {
                val lastDismissed = _uiState.value.lastDismissed
                if (lastDismissed != null) {
                    viewModelScope.launch {
                        restoreInsightUseCase(lastDismissed.id)
                        _uiState.update {
                            it.copy(
                                insights = it.insights + lastDismissed,
                                lastDismissed = null
                            )
                        }
                    }
                }
            }

            is InsightsEvent.ReviewClicked -> {
                val insight = _uiState.value.insights.find { it.id == event.insightId }
                // Find the subscription related to this insight (for UnusedSubscription type)
                // For now, we'll use the first subscription as a placeholder
                val subscription = _uiState.value.subscriptions.firstOrNull { it.status == SubscriptionStatus.Active }
                _uiState.update { it.copy(reviewTarget = subscription) }
            }

            InsightsEvent.ReviewClosed -> {
                _uiState.update { it.copy(reviewTarget = null) }
            }

            is InsightsEvent.MarkCancelled -> {
                viewModelScope.launch {
                    cancelSubscriptionUseCase(event.subscriptionId)
                    _uiState.update { state ->
                        val updatedSubscriptions = state.subscriptions.map { sub ->
                            if (sub.id == event.subscriptionId) {
                                sub.copy(status = SubscriptionStatus.Cancelled)
                            } else {
                                sub
                            }
                        }
                        val newMonthlyTotal = updatedSubscriptions
                            .filter { it.status == SubscriptionStatus.Active }
                            .sumOf { it.amountPaise }

                        state.copy(
                            subscriptions = updatedSubscriptions,
                            monthlySubscriptionTotalPaise = newMonthlyTotal,
                            reviewTarget = null
                        )
                    }
                }
            }

            InsightsEvent.Retry -> {
                loadData()
            }
        }
    }
}
