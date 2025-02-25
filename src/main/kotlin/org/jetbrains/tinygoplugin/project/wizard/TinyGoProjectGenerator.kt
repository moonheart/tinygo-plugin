package org.jetbrains.tinygoplugin.project.wizard

import com.goide.project.GoModuleBuilderBase
import com.goide.vgo.wizard.VgoModuleBuilder
import com.goide.vgo.wizard.VgoNewProjectSettings
import com.goide.wizard.GoProjectGenerator
import com.intellij.facet.ui.ValidationResult
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import org.jetbrains.tinygoplugin.TinyGoBundle
import org.jetbrains.tinygoplugin.configuration.TinyGoConfiguration
import org.jetbrains.tinygoplugin.icon.TinyGoPluginIcons
import org.jetbrains.tinygoplugin.sdk.TinyGoSdkVersion
import org.jetbrains.tinygoplugin.services.TinyGoInfoExtractor
import org.jetbrains.tinygoplugin.services.extractTinyGoInfo
import org.jetbrains.tinygoplugin.services.propagateGoFlags
import javax.swing.Icon

@Suppress("MagicNumber")
val tinyGoVersionWithModules = TinyGoSdkVersion(0, 14, 0)

private fun getProjectPresentableName() = TinyGoBundle.message("name")
private fun getProjectDescription(): String = TinyGoBundle.message("new.project.description")
private fun getProjectIcon(): Icon = TinyGoPluginIcons.TinyGoIcon

class TinyGoProjectGenerator : GoProjectGenerator<TinyGoNewProjectSettings>() {

    override fun getName(): String = getProjectPresentableName()

    override fun getDescription(): String = getProjectDescription()

    override fun getLogo(): Icon = getProjectIcon()

    override fun validate(baseDirPath: String): ValidationResult = ValidationResult.OK

    override fun doGenerateProject(
        project: Project,
        baseDir: VirtualFile,
        newProjectSettings: TinyGoNewProjectSettings,
        module: Module,
    ) = configureModule(newProjectSettings, project, module, baseDir.path)

    override fun createPeer(): ProjectGeneratorPeer<TinyGoNewProjectSettings> = TinyGoProjectGeneratorPeer()
}

class TinyGoModuleBuilder : GoModuleBuilderBase<TinyGoNewProjectSettings>(TinyGoProjectGeneratorPeer()) {

    override fun getPresentableName(): String = getProjectPresentableName()

    override fun getDescription(): String = getProjectDescription()

    override fun getNodeIcon(): Icon = getProjectIcon()

    override fun moduleCreated(module: Module, isCreatingNewProject: Boolean) =
        configureModule(settings, module.project, module, contentEntryPath)
}

private fun configureModule(
    newProjectSettings: TinyGoNewProjectSettings,
    project: Project,
    module: Module,
    contentRoot: String?
) {
    newProjectSettings.tinyGoSettings.saveState(project)
    extractTinyGoSettings(project, newProjectSettings.tinyGoSettings)
    if (newProjectSettings.tinyGoSettings.sdk.sdkVersion.isAtLeast(tinyGoVersionWithModules)) {
        VgoModuleBuilder.vgoModuleCreated(
            module,
            VgoNewProjectSettings(newProjectSettings.goSdk, emptyMap(), true),
            true,
            contentRoot
        )
    }
}

private fun extractTinyGoSettings(project: Project, tinyGoSettings: TinyGoConfiguration) {
    TinyGoInfoExtractor(project).extractTinyGoInfo(tinyGoSettings) { _, output ->
        tinyGoSettings.extractTinyGoInfo(output)
        tinyGoSettings.saveState(project)
        propagateGoFlags(project, tinyGoSettings)
    }
}
