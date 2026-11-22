package de.gymmane.scanner.updates

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import de.gymmane.scanner.ui.theme.Apricot
import de.gymmane.scanner.ui.theme.Field
import de.gymmane.scanner.ui.theme.Outline

/** Shared GitHub-flavoured Markdown presentation for downloaded and cached release notes. */
@Composable
fun ReleaseNotes(notes: String, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    // Keep the existing layout bound; the renderer parses asynchronously and remembers the result.
    val content = notes.take(30_000)
    val typography = MaterialTheme.typography
    val body = typography.bodyMedium
    Markdown(
        content = content,
        modifier = modifier.fillMaxWidth(),
        colors = markdownColor(text = color, codeBackground = Field, dividerColor = Outline),
        typography = markdownTypography(
            h1 = typography.headlineSmall,
            h2 = typography.titleLarge,
            h3 = typography.titleMedium,
            h4 = typography.titleSmall,
            h5 = typography.titleSmall,
            h6 = typography.titleSmall,
            text = body,
            paragraph = body,
            ordered = body,
            bullet = body,
            list = body,
            textLink = TextLinkStyles(SpanStyle(
                color = Apricot,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            )),
        ),
        imageTransformer = Coil2ImageTransformerImpl,
        error = { Text(content, modifier = it, color = color, style = body) },
    )
}
