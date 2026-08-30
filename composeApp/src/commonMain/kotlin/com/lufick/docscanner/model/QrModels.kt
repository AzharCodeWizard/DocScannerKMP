package com.lufick.docscanner.model

enum class QrContentType(val displayName: String) {
    URL("Website URL"),
    TEXT("Plain Text"),
    WIFI("Wi-Fi Network"),
    CONTACT("Contact Card"),
    UPI("UPI Payment")
}

data class WifiConfig(
    val ssid: String = "",
    val password: String = "",
    val encryptionType: String = "WPA"
) {
    fun toQrPayload(): String = "WIFI:T:$encryptionType;S:$ssid;P:$password;;"
}

data class ContactCard(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val organization: String = ""
) {
    fun toVCardPayload(): String =
        "BEGIN:VCARD\nVERSION:3.0\nN:$name\nFN:$name\nORG:$organization\nTEL:$phone\nEMAIL:$email\nEND:VCARD"
}

data class UpiConfig(
    val vpa: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val note: String = "Payment"
) {
    fun toUpiPayload(): String {
        val amtParam = if (amount.isNotBlank()) "&am=$amount" else ""
        return "upi://pay?pa=$vpa&pn=${payeeName.replace(" ", "%20")}$amtParam&tn=${note.replace(" ", "%20")}&cu=INR"
    }
}
