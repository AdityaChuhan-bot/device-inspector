package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.device.DeviceViewModel
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
    assertEquals("Device Inspector", appName)
  }

  @Test
  fun `test viewModel initialization and modules`() {
    try {
      val app = ApplicationProvider.getApplicationContext<Application>()
      val viewModel = DeviceViewModel(app)
      assertNotNull(viewModel)
      assertNotNull(viewModel.deviceSummary.value)
      assertNotNull(viewModel.cpuInfo.value)
      assertNotNull(viewModel.ramInfo.value)
      assertNotNull(viewModel.batteryInfo.value)
      assertNotNull(viewModel.storageInfo.value)
      assertNotNull(viewModel.networkInfo.value)
      assertNotNull(viewModel.sensorList.value)
      assertNotNull(viewModel.displayInfo.value)
    } catch (e: Throwable) {
      e.printStackTrace()
      throw e
    }
  }
}
