package com.example

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = AppApplication::class)
class MainActivityLaunchTest {

    @Test
    fun testMainActivityLaunchesWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                shadowOf(Looper.getMainLooper()).idle()
                assert(!activity.isFinishing)
                assert(!activity.isDestroyed)
            }
        }
    }

    @Test
    fun testMainActivityRemainsOpenAfterSplashTransition() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(3500))
                assert(!activity.isFinishing)
                assert(!activity.isDestroyed)
            }
        }
    }
}
