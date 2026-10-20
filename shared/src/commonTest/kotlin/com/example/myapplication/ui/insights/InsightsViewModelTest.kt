package com.example.myapplication.ui.insights

import com.example.myapplication.data.repository.FakeInsightsRepository
import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.model.InsightType
import com.example.myapplication.domain.model.Subscription
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.domain.repository.InsightsRepository
import com.example.myapplication.domain.usecase.CancelSubscriptionUseCase
import com.example.myapplication.domain.usecase.DismissInsightUseCase
import com.example.myapplication.domain.usecase.GetInsightsUseCase
import com.example.myapplication.domain.usecase.GetSubscriptionsUseCase
import com.example.myapplication.domain.usecase.RestoreInsightUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private lateinit var fakeInsightsRepository: FakeInsightsRepository
    private lateinit var getInsightsUseCase: GetInsightsUseCase
    private lateinit var dismissInsightUseCase: DismissInsightUseCase
    private lateinit var restoreInsightUseCase: RestoreInsightUseCase
    private lateinit var getSubscriptionsUseCase: GetSubscriptionsUseCase
    private lateinit var cancelSubscriptionUseCase: CancelSubscriptionUseCase
    private lateinit var viewModel: InsightsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeInsightsRepository = FakeInsightsRepository()
        getInsightsUseCase = GetInsightsUseCase(fakeInsightsRepository)
        dismissInsightUseCase = DismissInsightUseCase(fakeInsightsRepository)
        restoreInsightUseCase = RestoreInsightUseCase(fakeInsightsRepository)
        getSubscriptionsUseCase = GetSubscriptionsUseCase(fakeInsightsRepository)
        cancelSubscriptionUseCase = CancelSubscriptionUseCase(fakeInsightsRepository)
        viewModel = InsightsViewModel(
            getInsightsUseCase,
            dismissInsightUseCase,
            restoreInsightUseCase,
            getSubscriptionsUseCase,
            cancelSubscriptionUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads insights and subscriptions`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.insights.size)
        assertEquals(3, state.subscriptions.size)
        assertEquals(478_00L, state.monthlySubscriptionTotalPaise)
    }

    @Test
    fun `dismiss clicked removes insight and stores last dismissed`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialInsights = viewModel.uiState.value.insights
        val insightToDismiss = initialInsights[0]

        viewModel.onEvent(InsightsEvent.DismissClicked(insightToDismiss.id))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.insights.size)
        assertEquals(insightToDismiss, state.lastDismissed)
    }

    @Test
    fun `undo dismiss restores insight and clears last dismissed`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialInsights = viewModel.uiState.value.insights
        val insightToDismiss = initialInsights[0]

        viewModel.onEvent(InsightsEvent.DismissClicked(insightToDismiss.id))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(InsightsEvent.UndoDismiss)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.insights.size)
        assertNull(state.lastDismissed)
    }

    @Test
    fun `review clicked opens review sheet with subscription`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val insight = viewModel.uiState.value.insights[0]
        viewModel.onEvent(InsightsEvent.ReviewClicked(insight.id))

        val state = viewModel.uiState.value
        assertNotNull(state.reviewTarget)
        assertEquals("Swiggy One", state.reviewTarget?.name)
    }

    @Test
    fun `review closed clears review target`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val insight = viewModel.uiState.value.insights[0]
        viewModel.onEvent(InsightsEvent.ReviewClicked(insight.id))
        viewModel.onEvent(InsightsEvent.ReviewClosed)

        val state = viewModel.uiState.value
        assertNull(state.reviewTarget)
    }

    @Test
    fun `mark cancelled updates subscription status and total`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val subscription = viewModel.uiState.value.subscriptions[0]
        val initialTotal = viewModel.uiState.value.monthlySubscriptionTotalPaise

        viewModel.onEvent(InsightsEvent.MarkCancelled(subscription.id))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val cancelledSubscription = state.subscriptions.find { it.id == subscription.id }
        assertNotNull(cancelledSubscription)
        assertEquals(SubscriptionStatus.Cancelled, cancelledSubscription.status)
        assertTrue(state.monthlySubscriptionTotalPaise < initialTotal)
    }

    @Test
    fun `error scenario and retry flow`() = runTest {
        val errorRepository = object : InsightsRepository {
            var shouldFail = true
            override fun observeInsights(): Flow<List<Insight>> = flow {
                if (shouldFail) {
                    throw IllegalStateException("Network error")
                } else {
                    emit(emptyList())
                }
            }
            override suspend fun dismiss(id: String) {}
            override suspend fun restore(id: String) {}
            override fun observeSubscriptions(): Flow<List<Subscription>> = flowOf(emptyList())
            override suspend fun markCancelled(id: String) {}
        }

        val errorViewModel = InsightsViewModel(
            GetInsightsUseCase(errorRepository),
            DismissInsightUseCase(errorRepository),
            RestoreInsightUseCase(errorRepository),
            GetSubscriptionsUseCase(errorRepository),
            CancelSubscriptionUseCase(errorRepository)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        var state = errorViewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)

        errorRepository.shouldFail = false
        errorViewModel.onEvent(InsightsEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        state = errorViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
}
