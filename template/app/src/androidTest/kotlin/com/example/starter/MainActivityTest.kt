package com.example.starter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.starter.feature.sample.R as SampleR

/** Compiled by verifyStarter, run on a device with `connectedDebugAndroidTest`. */
@HiltAndroidTest
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() = hiltRule.inject()

    @Test
    fun launchesAndShowsSampleScreen() {
        val refresh = composeRule.activity.getString(SampleR.string.sample_refresh)
        composeRule.onNodeWithText(refresh).assertIsDisplayed()
    }
}
