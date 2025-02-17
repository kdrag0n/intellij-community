package org.jetbrains.completion.full.line.settings.ui.components

import com.intellij.lang.Language
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.OnePixelDivider
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.*
import org.jetbrains.completion.full.line.models.ModelType
import org.jetbrains.completion.full.line.settings.state.MLServerCompletionSettings
import org.jetbrains.completion.full.line.settings.ui.LANGUAGE_CHECKBOX_NAME
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JTextField
import kotlin.reflect.KMutableProperty0


// For some reason method intTextField() in com/intellij/ui/layout/Cell.kt
// throws java.lang.LinkageError: loader constraint violation: when resolving method,
// But it's copy works fine :/
fun Cell.intTextFieldFixed(binding: PropertyBinding<Int>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
  return textField(
    { binding.get().toString() },
    { value -> value.toIntOrNull()?.let { intValue -> binding.set(range?.let { intValue.coerceIn(it.first, it.last) } ?: intValue) } },
    columns
  ).withValidationOnInput {
    val value = it.text.toIntOrNull()
    if (value == null)
      error("Please enter a number")
    else if (range != null && value !in range)
      error("Please enter a number from ${range.first} to ${range.last}")
    else null
  }
}

fun Cell.doubleTextField(binding: PropertyBinding<Double>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
  return textField(
    { binding.get().toString() },
    { value ->
      value.toDoubleOrNull()
        ?.let { intValue -> binding.set(range?.let { intValue.coerceIn(it.first.toDouble(), it.last.toDouble()) } ?: intValue) }
    },
    columns
  ).withValidationOnInput {
    val value = it.text.toDoubleOrNull()
    if (value == null)
      error("Please enter a valid double number (ex. 3.14)")
    else if (range != null && (value < range.first || value > range.last))
      error("Please enter a number from ${range.first} to ${range.last}")
    else null
  }
}

fun Cell.doubleTextField(prop: KMutableProperty0<Double>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
  return doubleTextField(prop.toBinding(), columns, range)
}

fun Cell.intTextFieldFixed(prop: KMutableProperty0<Int>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
  return intTextFieldFixed(prop.toBinding(), columns, range)
}

fun Row.separatorRow(): Row {
  return row {
    component(SeparatorComponent(0, OnePixelDivider.BACKGROUND, null))
  }
}

fun languageComboBox(langPanel: DialogPanel): ComboBox<String> {
  return ComboBox<String>().apply {
    renderer = listCellRenderer { langId, _, _ ->
      text = Language.findLanguageByID(langId)?.displayName ?: ""
    }
    addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        (langPanel.layout as CardLayout).show(langPanel, it.item.toString())
      }
    }
  }
}

fun modelTypeComboBox(langPanel: DialogPanel): ComboBox<ModelType> {
  return ComboBox<ModelType>().apply {
    renderer = listCellRenderer { value, _, _ ->
      text = value.name
      icon = value.icon
    }
    addItemListener {
      if (it.stateChange == ItemEvent.SELECTED) {
        (langPanel.layout as CardLayout).show(langPanel, it.item.toString())
      }
    }
  }
}

fun languageCheckBox(language: Language, biggestLang: String?): JBCheckBox {
  return JBCheckBox(language.displayName, MLServerCompletionSettings.getInstance().getLangState(language).enabled).apply {
    minimumSize = Dimension(36 + getFontMetrics(font).stringWidth(biggestLang ?: language.id), preferredSize.height)
    name = LANGUAGE_CHECKBOX_NAME
  }
}

fun Cell.loadingStatus(loadingIcon: LoadingComponent): List<CellBuilder<JComponent>> {
  return listOf(
    component(loadingIcon.loadingIcon),
    component(loadingIcon.statusText),
  )
}

