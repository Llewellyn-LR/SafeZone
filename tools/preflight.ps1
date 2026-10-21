# SafeZone (Android/Java) preflight check - port of the Flutter patch's tools/preflight.ps1
#
# Run from the project root (the folder that contains settings.gradle):
#   powershell -ExecutionPolicy Bypass -File .\tools\preflight.ps1
#
# It only reads files. It never changes anything.

$script:failures = 0
$script:warnings = 0

function Ok($msg) { Write-Host "[ OK ]  $msg" -ForegroundColor Green }
function Fail($msg, $fix) {
  Write-Host "[FAIL]  $msg" -ForegroundColor Red
  if ($fix) { Write-Host "        fix: $fix" -ForegroundColor Yellow }
  $script:failures++
}
function Warn($msg, $fix) {
  Write-Host "[WARN]  $msg" -ForegroundColor Yellow
  if ($fix) { Write-Host "        fix: $fix" -ForegroundColor DarkYellow }
  $script:warnings++
}
function Check($cond, $msg, $fix) { if ($cond) { Ok $msg } else { Fail $msg $fix } }

if (-not (Test-Path 'settings.gradle')) {
  Write-Host "Run this from the project root (the folder containing settings.gradle)." -ForegroundColor Red
  exit 1
}

$src = 'app/src/main/java/com/safezone/app'

Write-Host "`n== 1. Source files present =="
foreach ($f in @(
  "$src/SafeZoneApp.java", "$src/ServiceLocator.java",
  "$src/model/WantedPerson.java", "$src/model/CrimeHotspot.java", "$src/model/CrimeReport.java",
  "$src/service/AuthService.java", "$src/service/MockAuthService.java", "$src/service/FirebaseAuthService.java",
  "$src/service/HotspotService.java", "$src/service/MockHotspotService.java", "$src/service/FirebaseHotspotService.java",
  "$src/service/ReportService.java", "$src/service/MockReportService.java", "$src/service/FirebaseReportService.java",
  "$src/service/NotificationService.java", "$src/service/LocationService.java",
  "$src/ui/auth/SplashActivity.java", "$src/ui/auth/LoginActivity.java", "$src/ui/auth/RegisterActivity.java",
  "$src/ui/home/MainActivity.java", "$src/ui/home/HomeFragment.java", "$src/ui/map/MapFragment.java",
  "$src/ui/alerts/AlertsFragment.java", "$src/ui/report/ReportFragment.java", "$src/ui/profile/ProfileFragment.java",
  "$src/ui/auth/RoleRouter.java", "$src/ui/auth/AuthFormViewModel.java", "$src/ui/report/ReportViewModel.java", "$src/ui/alerts/AlertsViewModel.java", "$src/ui/map/MapViewModel.java", "$src/service/DataError.java", "$src/util/RadiusFilter.java", "$src/service/NearbyFilter.java", "$src/ui/station/StationsActivity.java", "$src/ui/help/HelpActivity.java", "$src/ui/sos/SosActivity.java", "$src/ui/common/Event.java", "$src/ui/admin/ModerationQueueFragment.java", "$src/ui/admin/ApproveReportDialog.java",
  "$src/service/WantedPersonService.java", "$src/service/MockWantedPersonService.java",
  "$src/service/FirebaseWantedPersonService.java", "$src/service/StorageService.java",
  "$src/service/ModeratorService.java",
  "$src/ui/wanted/WantedPersonsListActivity.java",
  "$src/ui/admin/AdminActivity.java", "$src/ui/admin/AdminShellFragment.java",
  "$src/ui/admin/WantedPersonsAdminFragment.java", "$src/ui/admin/AddWantedPersonActivity.java"
)) { Check (Test-Path $f) "$f exists" "Re-extract the project over your folder and choose overwrite" }

if (Test-Path "$src/service/StorageService.java") {
  $ss = Get-Content "$src/service/StorageService.java" -Raw
  Check ($ss -match 'uploadWantedPersonPhoto') "StorageService has uploadWantedPersonPhoto" "Overwrite StorageService.java with the one from this project"
}

Write-Host "`n== 2. Dependencies in app/build.gradle =="
$gradle = Get-Content 'app/build.gradle' -Raw
foreach ($d in @('firebase-auth', 'firebase-firestore', 'firebase-storage', 'firebase-messaging', 'play-services-location', 'osmdroid', 'lifecycle-viewmodel', 'material', 'glide')) {
  Check ($gradle -match $d) "app/build.gradle declares $d" "Add the $d dependency"
}

Write-Host "`n== 3. Leftover default tests =="
$stray = @(Get-ChildItem -Path 'app/src/test','app/src/androidTest' -Recurse -Filter 'Example*.java' -ErrorAction SilentlyContinue)
if ($stray.Count -gt 0) {
  Fail "Android Studio's stock Example*Test.java files are present and may reference a different package: $($stray.Name -join ', ')" "Delete them"
} else { Ok "no stock Example*Test.java present" }

Write-Host "`n== 4. Firebase configuration =="
if (Test-Path 'app/google-services.json') {
  $gs = Get-Content 'app/google-services.json' -Raw
  Check (-not ($gs -match 'REPLACE_ME')) "google-services.json has no REPLACE_ME placeholders" "Re-download it from the Firebase console"
  Check ($gs -match '"storage_bucket":\s*"[^"]+"') "google-services.json has a storage_bucket value" "Enable Storage in the console FIRST (needs the Blaze plan), then re-download google-services.json"
  Check ($gs -match '"package_name":\s*"com.safezone.app"') "google-services.json is for package com.safezone.app" "The file registers a different package (task 0.1). In the Firebase console add a second Android app named exactly com.safezone.app, then download the new google-services.json into app/"
} else {
  Warn "app/google-services.json is missing - the app will build and run in MOCK mode only (moderator features need Firebase)." "Download google-services.json from the Firebase console into app/ (see FIREBASE_SETUP.md section 3)"
}

Write-Host "`n== 5. Android manifest =="
$m = Get-Content 'app/src/main/AndroidManifest.xml' -Raw
Check ($m -match 'android.permission.INTERNET') "INTERNET permission declared" 'Add <uses-permission android:name="android.permission.INTERNET"/> above <application>'
Check ($m -match 'ACCESS_FINE_LOCATION') "location permission declared (Map tab + report location capture)" 'Add <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>'
Check ($m -match 'POST_NOTIFICATIONS') "POST_NOTIFICATIONS declared (push alerts toggle, Android 13+)" 'Add <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>'
Check ($m -match 'FileProvider') "FileProvider declared (camera capture needs it)" "Restore the <provider> block from the project's AndroidManifest.xml"

Write-Host ""
if ($script:failures -eq 0) {
  Write-Host "Preflight passed: $($script:warnings) warning(s), 0 failure(s)." -ForegroundColor Green
  exit 0
} else {
  Write-Host "Preflight found $($script:failures) failure(s) and $($script:warnings) warning(s). Fix the [FAIL] lines first." -ForegroundColor Red
  exit 1
}
