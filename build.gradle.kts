plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ktlint) apply false
}

// JitPack 會執行根專案的 `publishToMavenLocal`，這裡將其轉發到實際發布模組。
tasks.register("publishToMavenLocal") {
    dependsOn(":aw-arch:publishToMavenLocal")
}
