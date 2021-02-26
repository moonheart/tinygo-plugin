package org.jetbrains.tinygoplugin.runconfig

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import org.jetbrains.tinygoplugin.ui.ConfigurationProvider
import org.jetbrains.tinygoplugin.ui.MappedGraphProperty
import org.jetbrains.tinygoplugin.ui.TinyGoPropertiesWrapper
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty1

class RunConfigurationWrapper(private val configurationProvider: ConfigurationProvider<RunSettings>) :
    TinyGoPropertiesWrapper(configurationProvider) {
    inner class RunConfigurationProperty<T>(
        prop: GraphProperty<T>,
        objProperty: KMutableProperty1<RunSettings, T>,
    ) : MappedGraphProperty<T, RunSettings>(prop, objProperty, configurationProvider, this)

    val mainFile = RunConfigurationProperty(
        prop = propertyGraph.graphProperty(configurationProvider.tinyGoSettings::mainFile),
        objProperty = RunSettings::mainFile
    )

    val cmdLineArguments = RunConfigurationProperty(
        prop = propertyGraph.graphProperty(configurationProvider.tinyGoSettings::cmdlineOptions),
        objProperty = RunSettings::cmdlineOptions
    )
}

class TinyGoRunConfigurationEditor(private val runConfiguration: TinyGoRunConfiguration) :
    SettingsEditor<TinyGoRunConfiguration>() {

    private val properties = RunConfigurationWrapper(runConfiguration)

    init {
        resetEditorFrom(runConfiguration)
    }

    override fun resetEditorFrom(configuration: TinyGoRunConfiguration) {
        runConfiguration.runConfig = configuration.runConfig
        properties.reset()
    }

    override fun applyEditorTo(tinyGoRunConfiguration: TinyGoRunConfiguration) {
        tinyGoRunConfiguration.runConfig = runConfiguration.runConfig.deepCopy()
    }

    override fun createEditor(): JComponent {
        return panel {
            row("TinyGo path") {
                textField(properties.tinygoSDKPath).enabled(false)
            }
            row("Target") {
                textField(properties.target).enabled(false)
            }
            row("Command line arguments") {
                textField(properties.cmdLineArguments).growPolicy(GrowPolicy.MEDIUM_TEXT)
            }
            row("Path to main") {
                val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
                textFieldWithBrowseButton(
                    property = properties.mainFile,
                    fileChooserDescriptor = fileChooserDescriptor
                )
            }
        }
    }
}
