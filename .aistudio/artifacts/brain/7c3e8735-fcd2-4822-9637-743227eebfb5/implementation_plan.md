# Individual App AAB Generation & Multi-Flavor Gradle Architecture

A unified Gradle Product Flavor architecture that produces individual Android App Bundles (.AAB) for each exam app (APPSC Group 2, APPSC Group 1, AP DSC Teacher, AP Police SI/Constable, Panchayat Secretary, RRB NTPC, SSC CGL) backed by a single Google Firebase Console project.

> [!IMPORTANT] User Review & Critical Decisions
> - **Single Firebase Console Setup**: All individual app flavors share the existing Firebase project (`gen-lang-client-0384860259`). Each app flavor gets its own unique Application ID registered under the same Firebase console project, generating a unified `google-services.json` containing client entries for all package names.
> - **Gradle Product Flavors**: Product flavors will be configured under `flavorDimensions += "exam"` in `app/build.gradle.kts`. Each flavor binds its unique `applicationId`, launcher `app_name`, and `TENANT_ID`.
> - **Individual AAB Creation**: Running `gradle bundle<Flavor>Release` or `gradle bundleRelease` will generate individual signed .AAB bundles for each respective app.

---

## 1. Overview & Core Concept

- **Goal**: Enable independent publishing of each exam app (APPSC Group 2, AP Police, AP DSC, etc.) to Google Play Store as standalone .AAB bundles while keeping code maintainability, real-time sync, and Firebase Authentication under a single Firebase project.
- **Key Capabilities**:
  - Building individual .AAB bundles for each app flavor independently or all flavors together.
  - Automatic tenant resolution in code based on the active build flavor (`BuildConfig.TENANT_ID`).
  - Unified Firebase Auth & Firestore backend shared across all flavors with isolated package-level application IDs.

---

## 2. User Experience & Visual Design

- **App Specificity**:
  - Each generated .AAB automatically sets the Android launcher label (`app_name`) and launcher icon for that specific exam.
  - On launch, the app defaults directly to that exam tenant's branding, primary theme colors, and specific syllabus/MCQs without requiring manual tenant selection.
- **Multi-App Multi-Tenant Compatibility**:
  - Super admin mode and multi-app switching capabilities remain available for testing and cross-management when navigating as Admin.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Gradle Product Flavors vs. Separate Repositories**
  - *Chosen Approach*: Gradle Product Flavors in a single codebase (`flavorDimensions += "exam"`).
  - *Why*: Eliminates code duplication and allows immediate updates, bug fixes, and feature enhancements across all apps simultaneously while producing separate .AAB binaries.

- **Decision 2: Single Firebase Console Project vs. Multiple Firebase Projects**
  - *Chosen Approach*: Single Firebase Console Project containing multiple registered Android package IDs.
  - *Why*: Simplifies Firebase Firestore security rules, authentication provider setup, and cloud storage management into a single console dashboard.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     SINGLE FIREBASE CONSOLE PROJECT                     │
│                     (gen-lang-client-0384860259)                        │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                         google-services.json
               (Contains client entries for all 6 package IDs)
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                    GRADLE PRODUCT FLAVOR BUILD ENGINE                   │
├─────────────────┬─────────────────┬─────────────────┬───────────────────┤
│  appscGroup2    │    policeSi     │    apDsc        │   panchayatSec    │
│  com.aistudio.  │  com.aistudio.  │  com.aistudio.  │   com.aistudio.   │
│  syllabus.appsc │  syllabus.police│  syllabus.dsc   │   syllabus.panch  │
└────────┬────────┴────────┬────────┴────────┬────────┴─────────┬─────────┘
         │                 │                 │                  │
         ▼                 ▼                 ▼                  ▼
┌─────────────────┐┌─────────────────┐┌─────────────────┐┌──────────────────┐
│  appscGroup2-   ││    policeSi-    ││     apDsc-      ││  panchayatSec-   │
│   release.aab   ││   release.aab   ││   release.aab   ││   release.aab    │
└─────────────────┘└─────────────────┘└─────────────────┘└──────────────────┘
```

### Flavor & Package ID Mapping

1. **APPSC Group 2**:
   - Flavor Name: `appsc`
   - Application ID: `com.aistudio.syllabus.appsc`
   - App Name: `APPSC Group 2`
   - Tenant ID: `tenant_appsc_group2`
2. **AP Police SI & Constable**:
   - Flavor Name: `police`
   - Application ID: `com.aistudio.syllabus.police`
   - App Name: `AP Police SI`
   - Tenant ID: `tenant_police_constable`
3. **AP DSC Teacher Recruitment**:
   - Flavor Name: `dsc`
   - Application ID: `com.aistudio.syllabus.dsc`
   - App Name: `AP DSC Teacher`
   - Tenant ID: `tenant_dsc_teacher`
4. **Panchayat Secretary**:
   - Flavor Name: `panchayat`
   - Application ID: `com.aistudio.syllabus.panchayat`
   - App Name: `Panchayat Sec`
   - Tenant ID: `tenant_panchayat_secretary`
5. **RRB NTPC & Railway**:
   - Flavor Name: `rrb`
   - Application ID: `com.aistudio.syllabus.rrb`
   - App Name: `RRB Railway`
   - Tenant ID: `tenant_rrb_ntpc`
6. **SSC CGL & CHSL**:
   - Flavor Name: `ssc`
   - Application ID: `com.aistudio.syllabus.ssc`
   - App Name: `SSC CGL Exam`
   - Tenant ID: `tenant_ssc_cgl`

---

## 5. Build Commands & Execution Plan

1. **Configure Product Flavors in `app/build.gradle.kts`**:
   Add `flavorDimensions += "exam"` and define the 6 product flavors with their respective `applicationId`, `resValue("string", "app_name", ...)`, and `buildConfigField("String", "TENANT_ID", ...)`.
2. **Update `SampleData.kt` Active App Resolver**:
   Ensure `SampleData` binds the default active app to `BuildConfig.TENANT_ID` at startup.
3. **Build Individual AAB Bundles**:
   - Build single app AAB: `gradle bundleAppscRelease`
   - Build all app AABs together: `gradle bundleRelease`
4. **Verify Compilation**:
   Run `compile_applet` to confirm zero build errors across all flavor configurations.
