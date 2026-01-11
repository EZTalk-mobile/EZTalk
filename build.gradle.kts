// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //noinspection AndroidGradlePluginVersion
    id("com.android.application") version "8.13.2" apply false
    //noinspection NewerVersionAvailable
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
<<<<<<< HEAD

=======
    id("com.android.library") version "8.13.2" apply false
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    // Firebase Google Services plugin (project-level)
    id("com.google.gms.google-services") version "4.4.4" apply false
    //noinspection GradleDependency
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
