package de.gymmane.scanner.updates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.gymmane.scanner.BuildConfig
import de.gymmane.scanner.R
import de.gymmane.scanner.ui.*
import de.gymmane.scanner.ui.theme.*
import kotlinx.coroutines.flow.*

fun apkInstallIntent(context: android.content.Context, apk: java.io.File): Intent {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
    return Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply { clipData = ClipData.newRawUri("update", uri) }
}

@Composable
fun UpdateInstallEffects(viewModel: UpdateViewModel) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) viewModel.resumePermission()
        else viewModel.installFailed(permission = true)
    }
    LaunchedEffect(viewModel, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.state.map { it.installReady }.distinctUntilChanged().filter { it }.collect {
                val apk = viewModel.consumeInstall() ?: return@collect
                try {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        if (viewModel.awaitingPermission) viewModel.installFailed(permission = true)
                        else {
                            viewModel.awaitPermission()
                            permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
                        }
                    } else {
                        context.startActivity(apkInstallIntent(context, apk))
                        viewModel.installerOpened()
                    }
                } catch (_: Exception) { viewModel.installFailed() }
            }
        }
    }
}

@Composable
fun UpdateSettings(viewModel: UpdateViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsCard {
        SettingsRow(Icons.Outlined.SystemUpdate, stringResource(R.string.update_auto), onClick = { viewModel.automatic(!state.automatic) }) {
            Switch(state.automatic, viewModel::automatic)
        }
        SettingsDivider()
        SettingsRow(Icons.Outlined.Refresh, stringResource(if (state.checking) R.string.update_checking else R.string.update_check),
            state.status?.let { stringResource(it) }, onClick = if (!state.checking && !state.downloading) ({ viewModel.check(true) }) else null) {
            if (state.checking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Apricot)
        }
    }
}

@Composable
fun UpdateOffer(viewModel: UpdateViewModel, allowed: Boolean) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UpdateOffer(state, allowed, viewModel::later, viewModel::update)
}

@Composable
internal fun UpdateOffer(state: UpdateState, allowed: Boolean, onLater: () -> Unit, onUpdate: () -> Unit) {
    MotionDialog(state.offer?.takeIf { allowed }) { release, motion ->
        AlertDialog(modifier = motion, containerColor = Surface, onDismissRequest = onLater,
            title = { Text(stringResource(R.string.update_available, release.tag)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel(stringResource(R.string.update_whats_new))
                    ReleaseNotes(release.notes.ifBlank { stringResource(R.string.update_no_notes) },
                        Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()), color = Muted)
                    if (state.downloading) {
                        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth(), color = Apricot, trackColor = Field)
                        Text(stringResource(R.string.update_downloading), style = MaterialTheme.typography.bodySmall)
                    }
                    state.downloadError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { DialogConfirm(stringResource(R.string.update_action), onUpdate, enabled = !state.downloading) },
            dismissButton = { DialogDismiss(stringResource(R.string.update_later), onLater) })
    }
}

@Composable
fun WhatsNewScreen(state: UpdateState, onContinue: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Background, contentColor = Ink) {
        ScreenEntrance("whats-new") {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Box(Modifier.padding(top = 16.dp).size(60.dp).background(Surface, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = Apricot, modifier = Modifier.size(28.dp))
                }
                Text(("GymMane Scanner " + BuildConfig.VERSION_NAME).uppercase(java.util.Locale.ROOT), color = Apricot,
                    style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 22.dp, bottom = 6.dp))
                Text(stringResource(R.string.update_whats_new), style = MaterialTheme.typography.headlineLarge)
                Box(Modifier.fillMaxWidth().weight(1f).padding(top = 20.dp)) {
                    Surface(Modifier.fillMaxWidth(), color = Surface, shape = RoundedCornerShape(24.dp)) {
                        val notesModifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)
                        val notes = state.notes?.takeIf { it.isNotBlank() }
                        if (notes != null) {
                            ReleaseNotes(notes, notesModifier)
                        } else {
                            Text(stringResource(R.string.update_local_notes), notesModifier, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                PrimaryButton(stringResource(R.string.update_done), onContinue, Modifier.fillMaxWidth().padding(top = 16.dp))
            }
        }
    }
}
