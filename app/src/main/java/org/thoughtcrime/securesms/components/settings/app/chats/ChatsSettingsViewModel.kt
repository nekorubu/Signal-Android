package org.thoughtcrime.securesms.components.settings.app.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.thoughtcrime.securesms.dependencies.AppDependencies
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.BackupUtil
import org.thoughtcrime.securesms.util.ConversationUtil
import org.thoughtcrime.securesms.util.TextSecurePreferences // JW: added
import org.thoughtcrime.securesms.util.ThrottledDebouncer
import org.thoughtcrime.securesms.util.UriUtils // JW: added
import org.thoughtcrime.securesms.util.livedata.Store

class ChatsSettingsViewModel @JvmOverloads constructor(
  private val repository: ChatsSettingsRepository = ChatsSettingsRepository()
) : ViewModel() {

  private val refreshDebouncer = ThrottledDebouncer(500L)

  private val store: Store<ChatsSettingsState> = Store(
    ChatsSettingsState(
      generateLinkPreviews = SignalStore.settings.isLinkPreviewsEnabled,
      useAddressBook = SignalStore.settings.isPreferSystemContactPhotos,
      keepMutedChatsArchived = SignalStore.settings.shouldKeepMutedChatsArchived(),
      useSystemEmoji = SignalStore.settings.isPreferSystemEmoji,
      enterKeySends = SignalStore.settings.isEnterKeySends,
      localBackupsEnabled = SignalStore.settings.isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(AppDependencies.application)
      // JW: added
      ,
      chatBackupsLocation = TextSecurePreferences.isBackupLocationRemovable(AppDependencies.application),
      chatBackupsLocationApi30 = UriUtils.getFullPathFromTreeUri(AppDependencies.application, SignalStore.settings.signalBackupDirectory),
      chatBackupZipfile = TextSecurePreferences.isRawBackupInZipfile(AppDependencies.application),
      chatBackupZipfilePlain = TextSecurePreferences.isPlainBackupInZipfile(AppDependencies.application),
      keepViewOnceMessages = TextSecurePreferences.isKeepViewOnceMessages(AppDependencies.application),
      ignoreRemoteDelete = TextSecurePreferences.isIgnoreRemoteDelete(AppDependencies.application),
      deleteMediaOnly = TextSecurePreferences.isDeleteMediaOnly(AppDependencies.application),
      googleMapType = TextSecurePreferences.getGoogleMapType(AppDependencies.application),
      whoCanAddYouToGroups = TextSecurePreferences.whoCanAddYouToGroups(AppDependencies.application)
    )
  )

  val state: LiveData<ChatsSettingsState> = store.stateLiveData

  fun setGenerateLinkPreviewsEnabled(enabled: Boolean) {
    store.update { it.copy(generateLinkPreviews = enabled) }
    SignalStore.settings.isLinkPreviewsEnabled = enabled
    repository.syncLinkPreviewsState()
  }

  fun setUseAddressBook(enabled: Boolean) {
    store.update { it.copy(useAddressBook = enabled) }
    refreshDebouncer.publish { ConversationUtil.refreshRecipientShortcuts() }
    SignalStore.settings.isPreferSystemContactPhotos = enabled
    repository.syncPreferSystemContactPhotos()
  }

  fun setKeepMutedChatsArchived(enabled: Boolean) {
    store.update { it.copy(keepMutedChatsArchived = enabled) }
    SignalStore.settings.setKeepMutedChatsArchived(enabled)
    repository.syncKeepMutedChatsArchivedState()
  }

  fun setUseSystemEmoji(enabled: Boolean) {
    store.update { it.copy(useSystemEmoji = enabled) }
    SignalStore.settings.isPreferSystemEmoji = enabled
  }

  fun setEnterKeySends(enabled: Boolean) {
    store.update { it.copy(enterKeySends = enabled) }
    SignalStore.settings.isEnterKeySends = enabled
  }

  fun refresh() {
    val backupsEnabled = SignalStore.settings.isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(AppDependencies.application)

    if (store.state.localBackupsEnabled != backupsEnabled
    ) {
      store.update { it.copy(localBackupsEnabled = backupsEnabled) }
    }
    // JW: added. This is required to update the UI for settings that are not in the Signal store but in the shared preferences.
    store.update { getState().copy() }
  }

  // JW: added
  fun setChatBackupLocation(enabled: Boolean) {
    TextSecurePreferences.setBackupLocationRemovable(AppDependencies.application, enabled)
    TextSecurePreferences.setBackupLocationChanged(AppDependencies.application, true) // Used in BackupUtil.getAllBackupsNewestFirst()
    refresh()
  }

  // JW: added
  fun setChatBackupLocationApi30(value: String) {
    refresh()
  }

  // JW: added
  fun setChatBackupZipfile(enabled: Boolean) {
    TextSecurePreferences.setRawBackupZipfile(AppDependencies.application, enabled)
    refresh()
  }

  // JW: added
  fun setChatBackupZipfilePlain(enabled: Boolean) {
    TextSecurePreferences.setPlainBackupZipfile(AppDependencies.application, enabled)
    refresh()
  }

  // JW: added
  fun keepViewOnceMessages(enabled: Boolean) {
    TextSecurePreferences.setKeepViewOnceMessages(AppDependencies.application, enabled)
    refresh()
  }

  // JW: added
  fun ignoreRemoteDelete(enabled: Boolean) {
    TextSecurePreferences.setIgnoreRemoteDelete(AppDependencies.application, enabled)
    refresh()
  }

  // JW: added
  fun deleteMediaOnly(enabled: Boolean) {
    TextSecurePreferences.setDeleteMediaOnly(AppDependencies.application, enabled)
    refresh()
  }

  // JW: added
  fun setGoogleMapType(mapType: String) {
    TextSecurePreferences.setGoogleMapType(AppDependencies.application, mapType)
    refresh()
  }

  // JW: added
  fun setWhoCanAddYouToGroups(adder: String) {
    TextSecurePreferences.setWhoCanAddYouToGroups(AppDependencies.application, adder)
    refresh()
  }

  // JW: added
  private fun getState() = ChatsSettingsState(
    generateLinkPreviews = SignalStore.settings.isLinkPreviewsEnabled,
    useAddressBook = SignalStore.settings.isPreferSystemContactPhotos,
    keepMutedChatsArchived = SignalStore.settings.shouldKeepMutedChatsArchived(),
    useSystemEmoji = SignalStore.settings.isPreferSystemEmoji,
    enterKeySends = SignalStore.settings.isEnterKeySends,
    localBackupsEnabled = SignalStore.settings.isBackupEnabled,
    chatBackupsLocationApi30 = UriUtils.getFullPathFromTreeUri(AppDependencies.application, SignalStore.settings.signalBackupDirectory),
    chatBackupsLocation = TextSecurePreferences.isBackupLocationRemovable(AppDependencies.application),
    chatBackupZipfile = TextSecurePreferences.isRawBackupInZipfile(AppDependencies.application),
    chatBackupZipfilePlain = TextSecurePreferences.isPlainBackupInZipfile(AppDependencies.application),
    keepViewOnceMessages = TextSecurePreferences.isKeepViewOnceMessages(AppDependencies.application),
    ignoreRemoteDelete = TextSecurePreferences.isIgnoreRemoteDelete(AppDependencies.application),
    deleteMediaOnly = TextSecurePreferences.isDeleteMediaOnly(AppDependencies.application),
    googleMapType = TextSecurePreferences.getGoogleMapType(AppDependencies.application),
    whoCanAddYouToGroups = TextSecurePreferences.whoCanAddYouToGroups(AppDependencies.application)
  )
}
