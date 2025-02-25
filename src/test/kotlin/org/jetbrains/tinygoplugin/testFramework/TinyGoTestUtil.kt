package org.jetbrains.tinygoplugin.testFramework

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.UsefulTestCase
import org.jetbrains.tinygoplugin.configuration.TinyGoConfiguration
import org.jetbrains.tinygoplugin.sdk.TinyGoSdk
import org.jetbrains.tinygoplugin.sdk.TinyGoSdkVersion
import java.util.function.Consumer

fun setupTinyGo(
    testCase: UsefulTestCase,
    project: Project,
    customConfSupplier: Consumer<TinyGoConfiguration>? = null
): TinyGoConfiguration {
    val homePath = System.getProperty("user.dir") + "/src/test/testData/mockTinyGo"
    VfsRootAccess.allowRootAccess(testCase.testRootDisposable, homePath)
    val homeUrl = VfsUtil.pathToUrl(homePath)
    val tinyGoSettings = TinyGoConfiguration.getInstance(project)
    tinyGoSettings.sdk = TinyGoSdk(homeUrl, TinyGoSdkVersion(0, 19, 0))
    tinyGoSettings.targetPlatform = ""
    customConfSupplier?.accept(tinyGoSettings)
    tinyGoSettings.saveState(project)

    return tinyGoSettings
}
