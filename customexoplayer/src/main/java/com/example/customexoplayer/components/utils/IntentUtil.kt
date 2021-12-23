
package com.example.customexoplayer.components.utils

import android.content.Intent
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.*
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Util
import com.google.common.collect.ImmutableList
import java.util.*

/** Util to read from and populate an intent.  */
object IntentUtil {
    // Actions.
    const val ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW"
    const val ACTION_VIEW_LIST = "com.google.android.exoplayer.demo.action.VIEW_LIST"

    // Activity extras.
    const val PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders"

    // Media item configuration extras.
    const val URI_EXTRA = "uri"
    const val TITLE_EXTRA = "title"
    const val MIME_TYPE_EXTRA = "mime_type"
    const val CLIP_START_POSITION_MS_EXTRA = "clip_start_position_ms"
    const val CLIP_END_POSITION_MS_EXTRA = "clip_end_position_ms"
    const val AD_TAG_URI_EXTRA = "ad_tag_uri"
    const val DRM_SCHEME_EXTRA = "drm_scheme"
    const val DRM_LICENSE_URI_EXTRA = "drm_license_uri"
    const val DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties"
    const val DRM_SESSION_FOR_CLEAR_CONTENT = "drm_session_for_clear_content"
    const val DRM_MULTI_SESSION_EXTRA = "drm_multi_session"
    const val DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA = "drm_force_default_license_uri"
    const val SUBTITLE_URI_EXTRA = "subtitle_uri"
    const val SUBTITLE_MIME_TYPE_EXTRA = "subtitle_mime_type"
    const val SUBTITLE_LANGUAGE_EXTRA = "subtitle_language"

    /** Creates a list of [media items][MediaItem] from an [Intent].  */
    fun createMediaItemsFromIntent(intent: Intent): List<MediaItem> {
        val mediaItems: MutableList<MediaItem> = ArrayList()
        if (ACTION_VIEW_LIST == intent.action) {
            var index = 0
            while (intent.hasExtra(URI_EXTRA + "_" + index)) {
                val uri = Uri.parse(intent.getStringExtra(URI_EXTRA + "_" + index))
                mediaItems.add(
                    createMediaItemFromIntent(
                        uri, intent,  /* extrasKeySuffix= */
                        "_$index"
                    )
                )
                index++
            }
        } else {
            val uri = intent.data
            mediaItems.add(createMediaItemFromIntent(uri, intent,  /* extrasKeySuffix= */""))
        }
        return mediaItems
    }

    /** Populates the intent with the given list of [media items][MediaItem].  */
    fun addToIntent(mediaItems: List<MediaItem>, intent: Intent) {
        Assertions.checkArgument(!mediaItems.isEmpty())
        if (mediaItems.size == 1) {
            val mediaItem = mediaItems[0]
            val localConfiguration = Assertions.checkNotNull(mediaItem.localConfiguration)
            intent.setAction(ACTION_VIEW).data = mediaItem.localConfiguration!!.uri
            if (mediaItem.mediaMetadata.title != null) {
                intent.putExtra(TITLE_EXTRA, mediaItem.mediaMetadata.title)
            }
            addPlaybackPropertiesToIntent(localConfiguration, intent,  /* extrasKeySuffix= */"")
            addClippingConfigurationToIntent(
                mediaItem.clippingConfiguration, intent,  /* extrasKeySuffix= */""
            )
        } else {
            intent.action = ACTION_VIEW_LIST
            for (i in mediaItems.indices) {
                val mediaItem = mediaItems[i]
                val localConfiguration = Assertions.checkNotNull(mediaItem.localConfiguration)
                intent.putExtra(URI_EXTRA + "_$i", localConfiguration.uri.toString())
                addPlaybackPropertiesToIntent(
                    localConfiguration, intent,  /* extrasKeySuffix= */
                    "_$i"
                )
                addClippingConfigurationToIntent(
                    mediaItem.clippingConfiguration, intent,  /* extrasKeySuffix= */"_$i"
                )
                if (mediaItem.mediaMetadata.title != null) {
                    intent.putExtra(TITLE_EXTRA + "_$i", mediaItem.mediaMetadata.title)
                }
            }
        }
    }

    private fun createMediaItemFromIntent(
        uri: Uri?, intent: Intent, extrasKeySuffix: String
    ): MediaItem {
        val mimeType = intent.getStringExtra(MIME_TYPE_EXTRA + extrasKeySuffix)
        val title = intent.getStringExtra(TITLE_EXTRA + extrasKeySuffix)
        val adTagUri = intent.getStringExtra(AD_TAG_URI_EXTRA + extrasKeySuffix)
        val subtitleConfiguration = createSubtitleConfiguration(intent, extrasKeySuffix)
        val builder = MediaItem.Builder()
            .setUri(uri)
            .setMimeType(mimeType)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
            .setClippingConfiguration(
                ClippingConfiguration.Builder()
                    .setStartPositionMs(
                        intent.getLongExtra(CLIP_START_POSITION_MS_EXTRA + extrasKeySuffix, 0)
                    )
                    .setEndPositionMs(
                        intent.getLongExtra(
                            CLIP_END_POSITION_MS_EXTRA + extrasKeySuffix, C.TIME_END_OF_SOURCE
                        )
                    )
                    .build()
            )
        if (adTagUri != null) {
            builder.setAdsConfiguration(
                AdsConfiguration.Builder(Uri.parse(adTagUri)).build()
            )
        }
        if (subtitleConfiguration != null) {
            builder.setSubtitleConfigurations(ImmutableList.of(subtitleConfiguration))
        }
        return populateDrmPropertiesFromIntent(builder, intent, extrasKeySuffix).build()
    }

    private fun createSubtitleConfiguration(
        intent: Intent, extrasKeySuffix: String
    ): SubtitleConfiguration? {
        return if (!intent.hasExtra(SUBTITLE_URI_EXTRA + extrasKeySuffix)) {
            null
        } else SubtitleConfiguration.Builder(
            Uri.parse(intent.getStringExtra(SUBTITLE_URI_EXTRA + extrasKeySuffix))
        )
            .setMimeType(
                Assertions.checkNotNull(
                    intent.getStringExtra(
                        SUBTITLE_MIME_TYPE_EXTRA + extrasKeySuffix
                    )
                )
            )
            .setLanguage(intent.getStringExtra(SUBTITLE_LANGUAGE_EXTRA + extrasKeySuffix))
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
    }

    private fun populateDrmPropertiesFromIntent(
        builder: MediaItem.Builder, intent: Intent, extrasKeySuffix: String
    ): MediaItem.Builder {
        val schemeKey = DRM_SCHEME_EXTRA + extrasKeySuffix
        val drmSchemeExtra = intent.getStringExtra(schemeKey) ?: return builder
        val headers: MutableMap<String, String> = HashMap()
        val keyRequestPropertiesArray = intent.getStringArrayExtra(
            DRM_KEY_REQUEST_PROPERTIES_EXTRA + extrasKeySuffix
        )
        if (keyRequestPropertiesArray != null) {
            var i = 0
            while (i < keyRequestPropertiesArray.size) {
                headers[keyRequestPropertiesArray[i]] = keyRequestPropertiesArray[i + 1]
                i += 2
            }
        }
        val drmUuid = Util.getDrmUuid(Util.castNonNull(drmSchemeExtra))
        if (drmUuid != null) {
            builder.setDrmConfiguration(
                DrmConfiguration.Builder(drmUuid)
                    .setLicenseUri(intent.getStringExtra(DRM_LICENSE_URI_EXTRA + extrasKeySuffix))
                    .setMultiSession(
                        intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA + extrasKeySuffix, false)
                    )
                    .setForceDefaultLicenseUri(
                        intent.getBooleanExtra(
                            DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA + extrasKeySuffix, false
                        )
                    )
                    .setLicenseRequestHeaders(headers)
                    .forceSessionsForAudioAndVideoTracks(
                        intent.getBooleanExtra(
                            DRM_SESSION_FOR_CLEAR_CONTENT + extrasKeySuffix,
                            false
                        )
                    )
                    .build()
            )
        }
        return builder
    }

    private fun addPlaybackPropertiesToIntent(
        localConfiguration: LocalConfiguration, intent: Intent, extrasKeySuffix: String
    ) {
        intent
            .putExtra(MIME_TYPE_EXTRA + extrasKeySuffix, localConfiguration.mimeType)
            .putExtra(
                AD_TAG_URI_EXTRA + extrasKeySuffix,
                if (localConfiguration.adsConfiguration != null) localConfiguration.adsConfiguration!!.adTagUri.toString() else null
            )
        if (localConfiguration.drmConfiguration != null) {
            addDrmConfigurationToIntent(
                localConfiguration.drmConfiguration,
                intent,
                extrasKeySuffix
            )
        }
        if (!localConfiguration.subtitleConfigurations.isEmpty()) {
            Assertions.checkState(localConfiguration.subtitleConfigurations.size == 1)
            val subtitleConfiguration = localConfiguration.subtitleConfigurations[0]
            intent.putExtra(
                SUBTITLE_URI_EXTRA + extrasKeySuffix,
                subtitleConfiguration.uri.toString()
            )
            intent.putExtra(
                SUBTITLE_MIME_TYPE_EXTRA + extrasKeySuffix,
                subtitleConfiguration.mimeType
            )
            intent.putExtra(
                SUBTITLE_LANGUAGE_EXTRA + extrasKeySuffix,
                subtitleConfiguration.language
            )
        }
    }

    private fun addDrmConfigurationToIntent(
        drmConfiguration: DrmConfiguration?, intent: Intent, extrasKeySuffix: String
    ) {
        intent.putExtra(DRM_SCHEME_EXTRA + extrasKeySuffix, drmConfiguration!!.scheme.toString())
        intent.putExtra(
            DRM_LICENSE_URI_EXTRA + extrasKeySuffix,
            if (drmConfiguration.licenseUri != null) drmConfiguration.licenseUri.toString() else null
        )
        intent.putExtra(DRM_MULTI_SESSION_EXTRA + extrasKeySuffix, drmConfiguration.multiSession)
        intent.putExtra(
            DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA + extrasKeySuffix,
            drmConfiguration.forceDefaultLicenseUri
        )
        val drmKeyRequestProperties = arrayOfNulls<String>(
            drmConfiguration.licenseRequestHeaders.size * 2
        )
        var index = 0
        for ((key, value) in drmConfiguration.licenseRequestHeaders) {
            drmKeyRequestProperties[index++] = key
            drmKeyRequestProperties[index++] = value
        }
        intent.putExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA + extrasKeySuffix, drmKeyRequestProperties)
        val forcedDrmSessionTrackTypes: List<Int> = drmConfiguration.forcedSessionTrackTypes
        if (!forcedDrmSessionTrackTypes.isEmpty()) {
            // Only video and audio together are supported.
            Assertions.checkState(
                forcedDrmSessionTrackTypes.size == 2 && forcedDrmSessionTrackTypes.contains(C.TRACK_TYPE_VIDEO)
                        && forcedDrmSessionTrackTypes.contains(C.TRACK_TYPE_AUDIO)
            )
            intent.putExtra(DRM_SESSION_FOR_CLEAR_CONTENT + extrasKeySuffix, true)
        }
    }

    private fun addClippingConfigurationToIntent(
        clippingConfiguration: ClippingConfiguration,
        intent: Intent,
        extrasKeySuffix: String
    ) {
        if (clippingConfiguration.startPositionMs != 0L) {
            intent.putExtra(
                CLIP_START_POSITION_MS_EXTRA + extrasKeySuffix,
                clippingConfiguration.startPositionMs
            )
        }
        if (clippingConfiguration.endPositionMs != C.TIME_END_OF_SOURCE) {
            intent.putExtra(
                CLIP_END_POSITION_MS_EXTRA + extrasKeySuffix, clippingConfiguration.endPositionMs
            )
        }
    }
}