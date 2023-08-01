package com.gjg.myapplication

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke

/**
 * 测试设备管理配置
 */
internal fun configureGradleManagedDevices(commonExtension: CommonExtension<*,*,*,*>){
    val pixel4 = DeviceConfig("Pixel 4", 30, "aosp-atd")
    val pixel6 = DeviceConfig("Pixel 6", 31, "aosp")
    val pixelC = DeviceConfig("Pixel C", 30, "aosp-atd")

    val allDevices = listOf(pixel4, pixel6, pixelC)
    val ciDevices = listOf(pixel4, pixelC)

    //配置测试选项
    commonExtension.testOptions {
        //测试选项中的测试适配列表
        managedDevices {
            //统一测试平台的测试设备清单
            devices{
                allDevices.forEach {deviceConfig ->
                    //创建一个ManagedVirtualDevice对象，并对其进行配置
                    maybeCreate(deviceConfig.taskName,ManagedVirtualDevice::class.java).apply {
                        device = deviceConfig.device
                        apiLevel = deviceConfig.apiLevel
                        systemImageSource = deviceConfig.systemImageSource
                    }
                }
            }
            //测试设备组
            groups{
                ////创建一个名为"ci"的设备组，并对其进行配置
                maybeCreate("ci").apply {
                    ciDevices.forEach { deviceConfig ->
                        targetDevices.add(devices[deviceConfig.taskName])
                    }
                }
            }
        }
    }
}

private data class DeviceConfig(
    val device:String,
    val apiLevel: Int,
    val systemImageSource: String
){
    val taskName = buildString {
        append(device.lowercase().replace(" ",""))
        append("api")
        append(apiLevel.toString())
        append(systemImageSource.replace("-", ""))
    }
}