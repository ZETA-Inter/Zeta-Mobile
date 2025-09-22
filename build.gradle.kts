// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
<<<<<<< HEAD
    alias(libs.plugins.android.library) apply false
=======
    id("com.google.gms.google-services") version "4.4.3" apply false
>>>>>>> e1638fe2c76521e65b4ba56a85c7199323a253ac
}