package com.example.priorityping.model

import com.example.priorityping.R

data class SupportedApp(
    val appName: String,
    val displayName: String,
    val packageName: String,
    val identifierHint: String,
    val colorRes: Int,
    val iconRes: Int
)

object SupportedApps {
    val WHATSAPP = SupportedApp(
        appName = "whatsapp",
        displayName = "WhatsApp",
        packageName = "com.whatsapp",
        identifierHint = "Contact name (as shown in WhatsApp)",
        colorRes = R.color.whatsappGreen,
        iconRes = R.drawable.ic_whatsapp
    )

    val WHATSAPP_BUSINESS = SupportedApp(
        appName = "whatsapp_business",
        displayName = "WhatsApp Business",
        packageName = "com.whatsapp.w4b",
        identifierHint = "Contact name (as shown in WhatsApp)",
        colorRes = R.color.whatsappGreen,
        iconRes = R.drawable.ic_whatsapp
    )

    val INSTAGRAM = SupportedApp(
        appName = "instagram",
        displayName = "Instagram",
        packageName = "com.instagram.android",
        identifierHint = "Instagram username (without @)",
        colorRes = R.color.instagramPink,
        iconRes = R.drawable.ic_instagram
    )

    val SNAPCHAT = SupportedApp(
        appName = "snapchat",
        displayName = "Snapchat",
        packageName = "com.snapchat.android",
        identifierHint = "Snapchat display name",
        colorRes = R.color.snapchatYellow,
        iconRes = R.drawable.ic_snapchat
    )
    val MESSAGES = SupportedApp(
        appName = "Messages",
        displayName = "Messages",
        packageName = "com.samsung.android.apps.messaging",
        identifierHint = "Contact name (as shown in Messages)",
        colorRes = R.color.messagesBlue,
        iconRes = R.drawable.ic_messages
    )

    val ALL = listOf(WHATSAPP, WHATSAPP_BUSINESS, INSTAGRAM, SNAPCHAT, MESSAGES)

    fun fromAppName(name: String): SupportedApp? = ALL.find { it.appName == name }
    fun fromPackageName(packageName: String): SupportedApp? = ALL.find { it.packageName == packageName }
}
