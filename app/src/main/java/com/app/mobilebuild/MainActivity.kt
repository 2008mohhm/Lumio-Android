package com.app.mobilebuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

private data class AppLanguage(val tag: String, val label: String)

private val supportedLanguages = listOf(
    AppLanguage("ar", "العربية"),
    AppLanguage("en", "الإنجليزية"),
    AppLanguage("fr", "الفرنسية"),
    AppLanguage("es", "الإسبانية"),
    AppLanguage("de", "الألمانية"),
    AppLanguage("tr", "التركية"),
    AppLanguage("it", "الإيطالية"),
    AppLanguage("pt", "البرتغالية"),
    AppLanguage("ru", "الروسية"),
    AppLanguage("hi", "الهندية"),
    AppLanguage("ja", "اليابانية"),
    AppLanguage("zh", "الصينية")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProviderLayout {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LumioApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun CompositionLocalProviderLayout(content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        content = content
    )
}

@Composable
private fun LumioApp() {
    var showWelcome by rememberSaveable { mutableStateOf(true) }

    if (showWelcome) {
        WelcomeScreen(onContinue = { showWelcome = false })
    } else {
        HomeScreen()
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "مرحبًا بك في Lumio",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ترجم النصوص بسهولة، مباشرة على جهازك.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onContinue) {
            Text(text = "ابدأ الآن")
        }
    }
}

@Composable
private fun HomeScreen() {
    var inputText by rememberSaveable { mutableStateOf("") }
    var translatedText by rememberSaveable { mutableStateOf("") }
    var sourceTag by rememberSaveable { mutableStateOf("ar") }
    var targetTag by rememberSaveable { mutableStateOf("en") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val sourceLanguage = supportedLanguages.first { it.tag == sourceTag }
    val targetLanguage = supportedLanguages.first { it.tag == targetTag }
    val translator = remember(sourceTag, targetTag) {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceTag)
                .setTargetLanguage(targetTag)
                .build()
        )
    }

    DisposableEffect(translator) {
        onDispose { translator.close() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lumio",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ترجمة نصوصك بسهولة وعلى جهازك",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageSelector(
                title = "من اللغة",
                selected = sourceLanguage,
                modifier = Modifier.weight(1f),
                onSelect = { language ->
                    if (language.tag == targetTag) {
                        targetTag = sourceTag
                    }
                    sourceTag = language.tag
                    translatedText = ""
                    statusMessage = null
                }
            )
            LanguageSelector(
                title = "إلى اللغة",
                selected = targetLanguage,
                modifier = Modifier.weight(1f),
                onSelect = { language ->
                    if (language.tag == sourceTag) {
                        sourceTag = targetTag
                    }
                    targetTag = language.tag
                    translatedText = ""
                    statusMessage = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                statusMessage = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            label = { Text("النص المراد ترجمته") },
            placeholder = { Text("اكتب أو الصق النص هنا…") },
            minLines = 5,
            maxLines = 7,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    inputText = ""
                    translatedText = ""
                    statusMessage = null
                    isError = false
                },
                enabled = !isLoading && (inputText.isNotEmpty() || translatedText.isNotEmpty()),
                modifier = Modifier.weight(1f)
            ) {
                Text("مسح النص")
            }
            OutlinedButton(
                onClick = {
                    val previousSource = sourceTag
                    sourceTag = targetTag
                    targetTag = previousSource
                    translatedText = ""
                    statusMessage = null
                },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("تبديل اللغتين")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val textToTranslate = inputText.trim()
                if (textToTranslate.isEmpty()) {
                    isError = true
                    statusMessage = "أدخل نصًا أولًا لترجمته."
                    translatedText = ""
                } else if (sourceTag == targetTag) {
                    isError = true
                    statusMessage = "اختر لغتين مختلفتين للترجمة."
                } else {
                    isLoading = true
                    isError = false
                    statusMessage = "جارٍ تجهيز نموذج اللغة ثم ترجمة النص…"
                    translatedText = ""
                    try {
                        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
                            .addOnSuccessListener {
                                translator.translate(textToTranslate)
                                    .addOnSuccessListener { result ->
                                        translatedText = result
                                        isLoading = false
                                        statusMessage = "اكتملت الترجمة على الجهاز."
                                        isError = false
                                    }
                                    .addOnFailureListener { error ->
                                        isLoading = false
                                        isError = true
                                        statusMessage = error.localizedMessage
                                            ?: "تعذرت ترجمة النص. حاول مرة أخرى."
                                    }
                            }
                            .addOnFailureListener { error ->
                                isLoading = false
                                isError = true
                                statusMessage = error.localizedMessage
                                    ?: "تعذر تنزيل نموذج اللغة. تحقق من اتصال الإنترنت وحاول مجددًا."
                            }
                    } catch (error: Exception) {
                        isLoading = false
                        isError = true
                        statusMessage = error.localizedMessage
                            ?: "حدث خطأ غير متوقع أثناء بدء الترجمة."
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(0.dp))
                Text("  جارٍ الترجمة…")
            } else {
                Text("ترجم الآن")
            }
        }

        statusMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "الترجمة",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = translatedText.ifEmpty { "ستظهر الترجمة هنا." },
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (translatedText.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "تعمل الترجمة محليًا بعد تنزيل نموذج اللغة لأول مرة. يلزم اتصال بالإنترنت لتنزيل النموذج.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguageSelector(
    title: String,
    selected: AppLanguage,
    modifier: Modifier = Modifier,
    onSelect: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected.label, textAlign = TextAlign.Center)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            supportedLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.label) },
                    onClick = {
                        expanded = false
                        onSelect(language)
                    }
                )
            }
        }
    }
}
