package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Eclipse Drift", appName)
    }

    @Test
    fun `game repository initializes defaults correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        val modules = repo.getDefaultModules()
        val entities = repo.getDefaultEntities()
        val logs = repo.getDefaultLogs()

        assertEquals(7, modules.size)
        assertEquals(7, entities.size)
        assertEquals(5, logs.size)
        assertNotNull(modules.find { it.id == "mod_warp_drive" })
    }
}
