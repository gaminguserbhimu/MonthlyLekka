# Project Plan



## Project Brief



## Implementation Steps

### Task_1_UpdateAdMobIDs: Update AndroidManifest.xml with production AdMob App ID (ca-app-pub-4120760179761356~9180724083) and BannerAdView.kt with production Banner Ad Unit ID (ca-app-pub-4120760179761356/2871652402).
- **Status:** COMPLETED
- **Updates:** Updated AndroidManifest.xml with production AdMob App ID (ca-app-pub-4120760179761356~9180724083) and BannerAdView.kt with production Banner Ad Unit ID (ca-app-pub-4120760179761356/2871652402). Passed all 51 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - AndroidManifest.xml contains production AdMob App ID ca-app-pub-4120760179761356~9180724083
  - BannerAdView.kt contains default production adUnitId ca-app-pub-4120760179761356/2871652402

### Task_2_BuildReleaseAndPush: Assemble release build package, commit changes, and push updates to GitHub repository.
- **Status:** COMPLETED
- **Updates:** Assembled release build successfully. Committed and pushed production AdMob ID updates to GitHub.
- **Acceptance Criteria:**
  - Release build passes successfully
  - Git changes committed and pushed to GitHub

### Task_3_RunAndVerify: Run and verify the application. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Verified production AdMob App ID (ca-app-pub-4120760179761356~9180724083) in AndroidManifest.xml and production Banner Ad Unit ID (ca-app-pub-4120760179761356/2871652402) in BannerAdView.kt. Verified build and 51 unit tests passing. Pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - application verified for stability and requirements alignment

### Task_4_IntegrateInterstitialAds: Create InterstitialAdManager.kt helper component using production ID ca-app-pub-4120760179761356/1771535176, preload interstitial ad on launch in MainActivity.kt, and trigger ad show upon creating a table or exporting backup in TablesScreen.kt and HelpScreen.kt.
- **Status:** COMPLETED
- **Updates:** Completed Task 4. Created InterstitialAdManager.kt using production Interstitial Ad Unit ID ca-app-pub-4120760179761356/1771535176. Preloaded interstitial ad in MainActivity.kt on app launch. Triggered InterstitialAdManager.showAd() upon table creation in TablesScreen.kt and CSV/JSON backup export in TablesScreen.kt and HelpScreen.kt. Passed all 51 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - InterstitialAdManager created with production ad unit ID ca-app-pub-4120760179761356/1771535176
  - Interstitial ad preloaded on app launch in MainActivity.kt
  - Interstitial ad triggered upon table creation in TablesScreen.kt
  - Interstitial ad triggered upon CSV/JSON backup export in TablesScreen.kt and HelpScreen.kt
  - build pass

### Task_5_RunAndVerifyRelease: Assemble production release build package, commit changes, push to GitHub, and instruct critic_agent to verify application stability (no crashes) and alignment with requirements.
- **Status:** COMPLETED
- **Updates:** Verified InterstitialAdManager.kt with production Interstitial Ad Unit ID ca-app-pub-4120760179761356/1771535176. Verified ad preloading in MainActivity.kt on launch, and ad triggers on table creation and backup export in TablesScreen.kt and HelpScreen.kt. Assembled release build and pushed changes to GitHub.
- **Acceptance Criteria:**
  - Release build passes successfully
  - Git changes committed and pushed to GitHub
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - application verified for stability and requirements alignment

### Task_6_CleanMasterTableDuplicatesAndDefaultRestriction: Clean up duplicate Master Table records in Room DB on startup in ExpenseViewModel & LekkaDao, filter UI table dropdowns/lists in WelcomeScreen, TablesScreen, and AddExpenseScreen using distinctBy, prevent Master Table from being set as default in ExpenseViewModel, and hide default star button on Master Table card in TablesScreen.
- **Status:** COMPLETED
- **Updates:** Completed Task 6. Cleaned up duplicate Mother Table records in Room DB on startup in ExpenseViewModel.kt, ensured Master Table is strictly isDefault = false, and prevented Master Table from being set as default. Filtered table dropdowns/lists across WelcomeScreen.kt, TablesScreen.kt, and AddExpenseScreen.kt using distinctBy so Master Table is listed strictly only once. Passed all 53 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - Duplicate Master Table records cleaned up in Room DB on startup so strictly one Master Table exists
  - Table dropdowns/lists in WelcomeScreen, TablesScreen, AddExpenseScreen filter Master Table using distinctBy
  - Master Table is strictly isDefault=false and cannot be set as default in ExpenseViewModel
  - Set Default star icon is hidden on Master Table card in TablesScreen
  - build pass

### Task_7_RunVerifyAndPush: Run unit tests, verify build and application stability (no crashes), commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified duplicate Master Table DB record cleanup on startup in ExpenseViewModel.kt. Verified Master Table is strictly isDefault = false and cannot be set as default. Verified table dropdowns in WelcomeScreen.kt, TablesScreen.kt, and AddExpenseScreen.kt list Master Expense Table strictly ONLY ONCE using distinctBy. Verified Master Table card in TablesScreen.kt has no set-default star button. Passed all 53 unit tests and assembled release build. Pushed to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_8_FilterMasterTableAndEnsureChildActiveTable: Filter out Master Table (isMotherTable == true) from Active Table Selector Dropdown in WelcomeScreen.kt. Ensure ExpenseViewModel.kt active table state defaults to a child table (isMotherTable == false) and automatically switches away if pointing to Mother Table on startup.
- **Status:** COMPLETED
- **Updates:** Completed Task 8. Filtered out Master Table (isMotherTable == true) from Active Table Selector Dropdown in WelcomeScreen.kt and AddExpenseScreen.kt. Updated ExpenseViewModel.kt so activeLekka state strictly resolves to a child table (!isMotherTable) and automatically switches away from Mother Table on startup. Passed all 55 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - Master Table filtered out from Active Table Selector Dropdown in WelcomeScreen.kt
  - ExpenseViewModel active table state defaults to child table and switches away from Mother Table if active on startup
  - build pass

### Task_9_RunVerifyAndPush: Run unit tests, verify build and application stability (no crashes), commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified Master Expense Table is completely removed from the Active Table Selector dropdown in WelcomeScreen.kt and AddExpenseScreen.kt. Verified ExpenseViewModel.kt activeLekka state strictly resolves to child tables (!isMotherTable). Verified crash-free execution, passed all 55 unit tests, assembled release build, and pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_10_FixMasterTableAggregation: Fix Master Expense Table aggregation in ExpenseViewModel, TableDetailScreen, and WelcomeScreen so that when viewing/displaying Master Table (isMotherTable == true), transactions, income, outcome, and net balance are combined and calculated across ALL child tables.
- **Status:** COMPLETED
- **Updates:** Completed Task 10. Updated ExpenseViewModel.kt, AppRepository.kt, TableDetailScreen.kt, and WelcomeScreen.kt so that when viewing/displaying Master Table (isMotherTable == true), the expenses flow collects ALL transactions across ALL child tables, and income, outcome, net balance, and charts aggregate combined totals across all child tables. Passed all 56 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - When isMotherTable == true in ExpenseViewModel and TableDetailScreen, transactions list includes all expenses from all child tables
  - Income, Outcome, and Net Balance for Master Table sum expenses across all child tables
  - Master Table Overview Card in WelcomeScreen displays aggregated Net Balance, Income, and Outcome across all child tables
  - build pass

### Task_11_RunVerifyAndPush: Run unit tests, verify build and application stability (no crashes), commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified Master Expense Table aggregation across all child tables in ExpenseViewModel.kt, AppRepository.kt, TableDetailScreen.kt, and WelcomeScreen.kt. Confirmed that when isMotherTable == true, transactions, income, outcome, net balance, pie charts, and monthly/yearly tables aggregate combined totals across ALL child tables. Passed all 56 unit tests, assembled release build, and pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_12_RenameKannadaCategoryTerms: Rename Kannada category terms ('kaipalle'/'Kaipalle' -> 'Vegetables', 'kirani'/'Kirani' -> 'Groceries') across AppDatabase.kt, CategoryDao.kt, DefaultCategories.kt, and seeders. Implement automatic runtime Room DB cleanup in ExpenseViewModel.kt on startup. Audit codebase for leftover Kannada terms, and bump app version to v1.0.2 (versionCode = 3).
- **Status:** COMPLETED
- **Updates:** Completed Task 12. Renamed Kannada category terms Kaipalle/kaipalle to Vegetables and Kirani/kirani to Groceries across AppDatabase.kt, Category.kt, CategoryIconUtils.kt, and UI components. Implemented automatic runtime DB migration in ExpenseViewModel.kt updating legacy DB category names. Updated app version to v1.0.2 (versionCode = 3). Passed all 57 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - kaipalle/Kaipalle renamed to Vegetables and kirani/Kirani renamed to Groceries across seeders, DAO queries, and default categories
  - ExpenseViewModel automatically renames existing Room DB categories kaipalle/kirani to Vegetables/Groceries on startup
  - Codebase audited and free of leftover Kannada terms
  - App version updated to v1.0.2 with versionCode 3
  - build pass

### Task_13_RunVerifyAndPushRelease: Run unit tests, verify build and application stability (no crashes), assemble release APK v1.0.2, commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified category term renaming (kaipalle -> Vegetables, kirani -> Groceries) across AppDatabase.kt, Category.kt, CategoryIconUtils.kt, and UI screens. Verified automatic runtime DB migration in ExpenseViewModel.kt updating legacy categories. Verified versionCode = 3 and versionName = '1.0.2'. Assembled release build v1.0.2, passed all 57 unit tests, and pushed to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Release APK v1.0.2 assembled successfully
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_14_IntegrateVinAppsBrandingSplashAndAbout: Create VinApps company logo vector/drawable assets (ic_vinapps_logo.xml ringed planet emblem), implement animated SplashScreen.kt with Route.Splash (ringed planet emblem animation, typography, transition to Onboarding/Welcome after 1.5s), update Navigation/MainActivity, and integrate About VinApps section card in HelpScreen.kt.
- **Status:** COMPLETED
- **Updates:** Completed Task 14. Created ic_vinapps_planet.xml ringed planet logo vector asset. Implemented animated SplashScreen.kt (Route.Splash) with VinApps branding, planet emblem scale/fade animation, and 1.5s auto-transition to Onboarding/Welcome. Updated MainActivity.kt start destination. Integrated About VinApps section card in HelpScreen.kt with company logo emblem, developer credits, and version v1.0.2. Passed all 57 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - VinApps ringed planet logo vector asset created and added to drawables
  - Animated SplashScreen.kt implemented with Route.Splash and 1.5s automatic transition
  - About VinApps section card added to HelpScreen.kt with logo emblem, company name, developer credits, and version v1.0.2
  - build pass

### Task_15_RunVerifyAndPush: Run unit tests, verify build and application stability (no crashes), commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified VinApps company branding vector logo asset (ic_vinapps_planet.xml). Verified animated startup SplashScreen.kt (Route.Splash) with VinApps ringed planet emblem scale/alpha animation, typography, and 1.5s auto-transition in MainActivity.kt and NavGraph.kt. Verified About VinApps section card in HelpScreen.kt with company logo emblem, developer credits, version v1.0.2, and privacy statement. Passed all 57 unit tests, assembled release build v1.0.2, and pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_16_RedesignVinAppsCosmicBranding: Redesign ic_vinapps_planet.xml vector asset with multi-color cosmic metallic/neon palette (dark slate/navy planet body with silver stripes, neon cyan & electric violet orbital rings, neon lime & gold sparkles). Update SplashScreen.kt with deep cosmic navy-indigo background gradient (#0B0F19 -> #1E1B4B -> #0F172A) and neon cyan/electric violet typography accents. Update About VinApps card in HelpScreen.kt with cosmic glassmorphism styling, neon cyan subtitle, and multi-color metallic emblem.
- **Status:** COMPLETED
- **Updates:** Completed Task 16. Redesigned ic_vinapps_planet.xml vector asset with multi-color cosmic metallic/neon palette (metallic dark slate/navy body with silver stripes, neon cyan & electric violet orbital rings, neon lime & gold sparkles). Updated SplashScreen.kt with deep cosmic navy-indigo background gradient (#0F172A to #1E1B4B to #0F172A) and neon cyan/electric violet typography accents. Updated About VinApps card in HelpScreen.kt with cosmic navy-indigo glassmorphism background, cyan border, neon cyan tagline, and electric violet credits. Passed all 57 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - ic_vinapps_planet.xml vector asset redesigned with cosmic metallic/neon multi-color palette
  - SplashScreen.kt updated with deep cosmic navy-indigo background gradient and neon cyan/electric violet typography accents
  - HelpScreen.kt About VinApps card styled with cosmic navy/purple glassmorphism background and neon cyan subtitle
  - build pass

### Task_17_RunVerifyAndPushCosmicRefresh: Run unit tests, verify build and application stability (no crashes), commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified multi-color cosmic metallic/neon redesign for VinApps branding. Verified ic_vinapps_planet.xml vector asset updated with metallic dark slate/navy body, silver surface stripes, neon cyan & electric violet orbital rings, and neon lime & gold sparkles. Verified SplashScreen.kt background updated with deep cosmic navy-indigo gradient (#0F172A to #1E1B4B to #0F172A) and neon cyan/electric violet typography. Verified About VinApps card in HelpScreen.kt updated with cosmic navy-indigo glassmorphism container and cyan border. Passed all 57 unit tests, assembled release build v1.0.2, and pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment

### Task_18_RefactorFilterAnalyticsAndBumpVersion: Refactor Slide 3 Filter tab in TableDetailScreen.kt to default to Today's transactions (LocalDate.now()), replace dual date pickers with Base Date + Duration (Number of Days) selector (1, 2, 3, 5, 7, 14, 30, Custom days), update summary stats, net balance formula bar, and Category Distribution Pie Chart dynamically, and bump app version to v1.0.4 (versionCode = 5).
- **Status:** COMPLETED
- **Updates:** Completed Task 18. Refactored Slide 3 Filter tab in TableDetailScreen.kt to default strictly to Today's transactions (LocalDate.now(), 1 day). Replaced old dual date pickers with Base Date picker (with quick Today reset chip) and Duration selector (1, 2, 3, 5, 7, 14, 30, and Custom days). Added dynamic range indicator badge banner (Showing X Days: Base Date - End Date). Updated filtered transactions list, income/outcome summary formula bar, and Category Distribution Pie Chart. Updated app version to v1.0.4 (versionCode = 5). Passed all 58 unit tests and debug assemble build.
- **Acceptance Criteria:**
  - Filter tab in TableDetailScreen defaults to Today's transactions on initial load
  - Dual date pickers replaced with Base Date picker and Duration selector (1, 2, 3, 5, 7, 14, 30, Custom days)
  - Dynamic range helper badge displays selected range (e.g. Showing X Days: Start Date – End Date)
  - Filtered transactions list, total income, total outcome, net balance formula bar, and Category Distribution Pie Chart update based on Base Date + Duration window
  - App version updated to v1.0.4 with versionCode = 5
  - build pass

### Task_19_RunVerifyBuildReleaseAndPush: Run unit tests, verify build and application stability (no crashes), assemble release APK v1.0.4, commit changes, push updates to GitHub repository, and instruct critic_agent to verify alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified Filter tab refactoring in TableDetailScreen.kt. Confirmed default to Today's transactions (LocalDate.now(), 1 day). Confirmed replacement of dual date pickers with Base Date picker (with quick Today button) and Duration selector (1, 2, 3, 5, 7, 14, 30, and Custom days). Confirmed dynamic range badge banner, filtered list, summary bar, and Category Distribution Pie Chart updates. Verified versionCode = 5 and versionName = '1.0.4'. Assembled release APK v1.0.4, passed all 58 unit tests, and pushed changes to GitHub.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Release APK v1.0.4 assembled successfully
  - Git changes committed and pushed to GitHub
  - application verified for stability and requirements alignment
- **Duration:** N/A

