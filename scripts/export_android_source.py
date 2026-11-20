import os
import shutil

repo_root = os.path.abspath(".")
backup_dir = os.path.abspath("backup_android_files")
output_md = os.path.abspath("ANDROID_FULL_SOURCE_CODE.md")

# Complete list of all important Android files across source, resources, tests, and configuration
files_manifest = [
    # 1. Manifest & Gradle Configs
    ("android/app/src/main/AndroidManifest.xml", "AndroidManifest.xml", "xml", "Manifest: Permissions, Application & Activity Definitions"),
    ("android/app/build.gradle.kts", "build.gradle.kts", "kotlin", "App Build Config: Dependencies, Plugins & SDK Versions"),
    ("android/build.gradle.kts", "project-build.gradle.kts", "kotlin", "Root Project Build Config: Plugin Repositories & Buildscript"),
    ("android/settings.gradle.kts", "settings.gradle.kts", "kotlin", "Settings Config: Plugin Management & Module Includes"),
    ("android/gradle/libs.versions.toml", "libs.versions.toml", "toml", "Version Catalog: Managed Dependency Versions"),
    ("android/app/proguard-rules.pro", "proguard-rules.pro", "pro", "ProGuard Rules: Code Shrinking & Obfuscation Rules"),

    # 2. Core App & Lifecycle
    ("android/app/src/main/java/com/kidcurate/app/KidCurateApplication.kt", "KidCurateApplication.kt", "kotlin", "Application Class: Global App Context Initialization"),
    ("android/app/src/main/java/com/kidcurate/app/MainActivity.kt", "MainActivity.kt", "kotlin", "Single Activity: Window Setup, Orientation & Navigation Host"),
    ("android/app/src/main/java/com/kidcurate/app/KidViewModel.kt", "KidViewModel.kt", "kotlin", "ViewModel: Central State, Playlist Curation & Failover"),

    # 3. Data & Authentication
    ("android/app/src/main/java/com/kidcurate/app/data/ApiClient.kt", "data/ApiClient.kt", "kotlin", "Network Client: Retrofit, Auth Tokens & Server Failover"),
    ("android/app/src/main/java/com/kidcurate/app/data/AppPolicies.kt", "data/AppPolicies.kt", "kotlin", "App Policies: Tablet/Phone Layout Rules & Probe Timeouts"),
    ("android/app/src/main/java/com/kidcurate/app/data/GoogleCredentialAuth.kt", "data/GoogleCredentialAuth.kt", "kotlin", "Google Auth: Credential Manager One-Tap Sign In"),
    ("android/app/src/main/java/com/kidcurate/app/data/Models.kt", "data/Models.kt", "kotlin", "Data Models: Videos, Playlists, Channels & Analytics DTOs"),
    ("android/app/src/main/java/com/kidcurate/app/data/ParentBiometricAuth.kt", "data/ParentBiometricAuth.kt", "kotlin", "Biometric Auth: Fingerprint/Face Verification Gate"),
    ("android/app/src/main/java/com/kidcurate/app/data/ParentPreferences.kt", "data/ParentPreferences.kt", "kotlin", "Preferences: SharedPreferences Storage for PIN, Curfew & Progress"),

    # 4. Child Screens
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/ChildSelectScreen.kt", "ui/screens/ChildSelectScreen.kt", "kotlin", "Child Screen: Avatar Picker & Server Connection Status"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/TodaysPlaylistScreen.kt", "ui/screens/TodaysPlaylistScreen.kt", "kotlin", "Child Screen: Daily Queue, Curfew Notice & Limits"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/PlayerScreen.kt", "ui/screens/PlayerScreen.kt", "kotlin", "Child Screen: YouTube Player, Child Lock & Resume Engine"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/CompletionScreen.kt", "ui/screens/CompletionScreen.kt", "kotlin", "Child Screen: Playlist Finish Celebration Screen"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/PinEntryScreen.kt", "ui/screens/PinEntryScreen.kt", "kotlin", "Security Screen: Dedicated Fullscreen Parent PIN Keypad"),

    # 5. Parent Zone & Management Tabs
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/ParentZoneScreen.kt", "ui/screens/ParentZoneScreen.kt", "kotlin", "Parent Zone: Main Dashboard & Tab Navigation"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/parent/VideosTabContent.kt", "ui/screens/parent/VideosTabContent.kt", "kotlin", "Parent Tab: Queue Management & Video Approval"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/parent/SearchTabContent.kt", "ui/screens/parent/SearchTabContent.kt", "kotlin", "Parent Tab: YouTube Search & Channel Curation"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/parent/ProfilesTabContent.kt", "ui/screens/parent/ProfilesTabContent.kt", "kotlin", "Parent Tab: Child Profiles & Bedtime Curfew Settings"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/parent/HistoryTabContent.kt", "ui/screens/parent/HistoryTabContent.kt", "kotlin", "Parent Tab: Watch History & Screen Time Analytics"),
    ("android/app/src/main/java/com/kidcurate/app/ui/screens/parent/SecurityTabContent.kt", "ui/screens/parent/SecurityTabContent.kt", "kotlin", "Parent Tab: PIN Modification, Biometrics & Account Delete"),

    # 6. UI Components & Theme
    ("android/app/src/main/java/com/kidcurate/app/ui/components/ConfettiAnimation.kt", "ui/components/ConfettiAnimation.kt", "kotlin", "UI Component: Confetti Particle Celebration Canvas"),
    ("android/app/src/main/java/com/kidcurate/app/ui/components/OnboardingTourDialog.kt", "ui/components/OnboardingTourDialog.kt", "kotlin", "UI Component: Interactive Parent Guided Tour Dialog"),
    ("android/app/src/main/java/com/kidcurate/app/ui/components/SoundHaptics.kt", "ui/components/SoundHaptics.kt", "kotlin", "UI Component: Tactile Sound & Haptic Feedback"),
    ("android/app/src/main/java/com/kidcurate/app/ui/components/ThematicComponents.kt", "ui/components/ThematicComponents.kt", "kotlin", "UI Component: Parent Password Dialog & Theme Avatars"),
    ("android/app/src/main/java/com/kidcurate/app/ui/theme/Color.kt", "ui/theme/Color.kt", "kotlin", "Design System: Material3 Color Tokens"),
    ("android/app/src/main/java/com/kidcurate/app/ui/theme/Theme.kt", "ui/theme/Theme.kt", "kotlin", "Design System: KidCurate App Theme Composable"),

    # 7. Android XML Resources
    ("android/app/src/main/res/values/strings.xml", "res/values/strings.xml", "xml", "Resource: App Strings & Accessibility Labels"),
    ("android/app/src/main/res/values/colors.xml", "res/values/colors.xml", "xml", "Resource: Core Brand Colors & Palette XML"),
    ("android/app/src/main/res/values/themes.xml", "res/values/themes.xml", "xml", "Resource: Android System Splash & Base Themes"),
    ("android/app/src/main/res/xml/network_security_config.xml", "res/xml/network_security_config.xml", "xml", "Security: Network Security Config for Local & HTTPS Traffic"),

    # 8. Automated Unit Test Suites
    ("android/app/src/test/java/com/kidcurate/app/data/AppPoliciesTest.kt", "test/AppPoliciesTest.kt", "kotlin", "Unit Test: Responsive Layout & Connection Policy Assertions"),
    ("android/app/src/test/java/com/kidcurate/app/ui/PlaybackUsabilityTest.kt", "test/PlaybackUsabilityTest.kt", "kotlin", "Unit Test: Playback Usability & Child Lock State Verifications"),
]

# Ensure backup files exist in backup_android_files directory as well
for src_rel, dst_rel, _, _ in files_manifest:
    src_abs = os.path.join(repo_root, src_rel)
    dst_abs = os.path.join(backup_dir, dst_rel)
    if os.path.exists(src_abs):
        os.makedirs(os.path.dirname(dst_abs), exist_ok=True)
        shutil.copy2(src_abs, dst_abs)

# Build comprehensive markdown document
lines = []
lines.append("# KidCurate Complete Android Source Code & Configuration Archive")
lines.append("")
lines.append("This document contains the complete, unabbreviated source code of all 38 essential Android files in the KidCurate repository, including application code, data layers, user interface screens, components, XML resources, Gradle build scripts, and unit tests.")
lines.append("")
lines.append("## Table of Contents")
lines.append("")

for idx, (_, dst_rel, _, desc) in enumerate(files_manifest, 1):
    display_name = dst_rel.replace("\\", "/")
    anchor = display_name.lower().replace("/", "").replace(".", "").replace("-", "")
    lines.append(f"{idx}. [{display_name}](#{anchor}) - *{desc}*")

lines.append("")
lines.append("---")
lines.append("")

for idx, (src_rel, dst_rel, lang, desc) in enumerate(files_manifest, 1):
    src_abs = os.path.join(repo_root, src_rel)
    display_name = dst_rel.replace("\\", "/")
    anchor = display_name.lower().replace("/", "").replace(".", "").replace("-", "")
    
    lines.append(f'<a name="{anchor}"></a>')
    lines.append(f"## {idx}. {display_name}")
    lines.append(f"**Original Location:** `{src_rel}`")
    lines.append(f"**Description:** {desc}")
    lines.append("")
    lines.append(f"```{lang}")
    if os.path.exists(src_abs):
        with open(src_abs, "r", encoding="utf-8") as f:
            code = f.read()
            lines.append(code)
    else:
        lines.append(f"// File not found on disk: {src_rel}")
    lines.append("```")
    lines.append("")
    lines.append("---")
    lines.append("")

with open(output_md, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

file_size = os.path.getsize(output_md)
total_lines = len(open(output_md, "r", encoding="utf-8").readlines())
print(f"Generated: {output_md}")
print(f"Total Files Included: {len(files_manifest)}")
print(f"Total Size: {file_size:,} bytes")
print(f"Total Document Lines: {total_lines:,}")
