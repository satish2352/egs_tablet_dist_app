import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")

}

android {
    namespace = "com.sipl.egstabdistribution"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sipl.egstabdistribution"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "6.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val p = Properties()
            p.load(project.rootProject.file("local.properties").reader())
            val yourKey: String = p.getProperty("BASE_URL")
            buildConfigField("String", "BASE_URL", "\"$yourKey\"")

        }
        debug {
            val p = Properties()
            p.load(project.rootProject.file("local.properties").reader())
            val yourKey: String = p.getProperty("BASE_URL")
            buildConfigField("String", "BASE_URL", "\"$yourKey\"")
        }
    }
    buildFeatures{
        viewBinding=true
        buildConfig=true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.code.gson:gson:2.10.1")
    //room database
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.github.pwittchen:reactivenetwork-rx2:3.0.8")
    implementation ("com.guolindev.permissionx:permissionx:1.7.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("io.getstream:photoview:1.0.1")





    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    // Add the CameraX dependencies
    implementation ("androidx.camera:camera-camera2:1.3.2")

    //FOR IMAGE COMPRESSION
    implementation ("id.zelory:compressor:3.0.1")




}