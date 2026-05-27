package com.example

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createEmptyComposeRule()

  @Test
  fun testMainActivityLaunchAndNavigation() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      // Wait for Compose to render the dashboard screen
      composeTestRule.waitForIdle()

      // Navigate to the Vault screen
      composeTestRule.onNodeWithTag("nav_item_vault").performClick()
      composeTestRule.waitForIdle()

      // Navigate to the Planner screen
      composeTestRule.onNodeWithTag("nav_item_planner").performClick()
      composeTestRule.waitForIdle()

      // Navigate to the Timer screen
      composeTestRule.onNodeWithTag("nav_item_timer").performClick()
      composeTestRule.waitForIdle()

      // Navigate back to the Dashboard
      composeTestRule.onNodeWithTag("nav_item_dashboard").performClick()
      composeTestRule.waitForIdle()
    }
  }
}

