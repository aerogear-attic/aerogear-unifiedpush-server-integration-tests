package org.arquillian.spacelift.gradle.quickstart

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.text.StringReplacementTool;

class ShootAndShareQuickstartHelper {
    
    private static final String dependencies = """
      repositories {
        mavenCentral()
      }

      dependencies {
        compile 'org.jboss.aerogear:aerogear-android-authz:2.0.0@aar'
        compile 'org.jboss.aerogear:aerogear-android-core:2.0.0@aar'
        compile 'org.jboss.aerogear:aerogear-android-pipe:2.0.0@aar'
        compile 'org.jboss.aerogear:aerogear-android-store:2.0.0@aar'
        compile 'com.google.code.gson:gson:1.7.2'
      }
"""
    
    private static final String defaultConfig = """
      defaultConfig {
        versionCode Integer.parseInt("" + getVersionCodeFromManifest() + "0")
        minSdkVersion 16
        targetSdkVersion 21
      }
"""

    void patchBuildGradle(String buildGradle) {

        File script = new File(buildGradle)
        List<String> lines = []
        script.eachLine { lines.add(it) }

        File patched = new File(buildGradle)

        patched.withWriter { out ->
            for (int i = 0; i < lines.size(); i++) {
                out.println lines[i]
                
                if (lines[i].startsWith("android {")) {
                    out.println dependencies
                }
            }
        }

        patched = new File(buildGradle)
        lines = []
        patched.eachLine { lines.add(it) }

        patched.withWriter { out ->
            for (int i = 0; i < lines.size(); i++) {
                if (lines[i].contains("defaultConfig {")) {
                    out.println defaultConfig
                    i += 2
                } else {
                    out.println lines[i]
                }
            }
        }
    }
    
    ShootAndShareQuickstartHelper patchAppJs(String appJsPath, String googleClientId, String facebookClientId, String facebookClientSecret, String keyCloakAuthAddress) {
        Spacelift.task(StringReplacementTool).in(new File(appJsPath))
            .replace("<your client secret goes here.apps.googleusercontent.com>").with(googleClientId)
            .replace("<your client id goes here>").with(facebookClientId)
            .replace("<your client secret goes here>").with(facebookClientSecret)
            .replace("<location of keycloak server e.g. http://192.168.0.12:8080/auth>").with(keyCloakAuthAddress)
            .execute().await()

        this
    }

    ShootAndShareQuickstartHelper patchControllerJs(String controllerJsPath, String keyCloakShootRestAddress) {
        Spacelift.task(StringReplacementTool).in(new File(controllerJsPath))
            .replace("<location of keycloak server e.g. http://192.168.0.12:8080/shoot/rest/photos>").with(keyCloakShootRestAddress)
            .execute().await()

        this
    }
}
