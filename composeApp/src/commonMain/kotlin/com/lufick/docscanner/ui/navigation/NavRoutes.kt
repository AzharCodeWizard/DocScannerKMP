package com.lufick.docscanner.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Crop : Screen("crop")
    data object Filter : Screen("filter")
    data object DocumentDetail : Screen("doc_detail/{docId}") {
        fun createRoute(docId: String) = "doc_detail/$docId"
    }
    data object Ocr : Screen("ocr/{docId}") {
        fun createRoute(docId: String) = "ocr/$docId"
    }
    data object PdfTools : Screen("pdf_tools/{docId}") {
        fun createRoute(docId: String) = "pdf_tools/$docId"
    }
    data object IdCard : Screen("id_card")
}
