package com.gjg.myapplication

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.Project

/**
 * flavor 配置
 */
@Suppress("EnumEntryName")
enum class FlavorDimension {
    contentType
}
// 定义产品风味的枚举类 NiaFlavor，每个枚举值代表一个产品风味，包含维度和应用ID后缀
@Suppress("EnumEntryName")
enum class NiaFlavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    demo(FlavorDimension.contentType, applicationIdSuffix = ".demo"),
    prod(FlavorDimension.contentType, )
}

fun Project.configureFlavors(
    commonExtension: CommonExtension<*, *, *, *>,
    flavorConfigurationBlock: ProductFlavor.(flavor: NiaFlavor) -> Unit = {}
) {
    commonExtension.apply {
        // 添加维度
        flavorDimensions += FlavorDimension.contentType.name
        // 创建不同的产品风味
        productFlavors {
            NiaFlavor.values().forEach {
                create(it.name){
                    // 设置维度
                    dimension = it.dimension.name
                    // 调用外部传入的 flavorConfigurationBlock 来配置每个产品风味
                    flavorConfigurationBlock(this, it)
                    // 如果 commonExtension 是 ApplicationExtension 并且当前是 ApplicationProductFlavor，可以设置应用ID后缀
                    if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                        if (it.applicationIdSuffix != null) {
                            this.applicationIdSuffix = it.applicationIdSuffix
                        }
                    }
                }
            }
        }
    }
}