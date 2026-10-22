# Review guide

Read by every reviewer and by verification. Repository-specific rules here win over the reviewer's general instructions.

## What this repository is

privo3-android-sdk is the Kotlin library partner apps embed to run the PRIVO age gate, age verification, identity verification and login on Android. It is one Gradle module, `sdk`, namespace `com.privo.sdk`, consumed from JitPack as `com.github.Privo:privo3-android-sdk:<tag>`; tags are cut by `ops/privo3/pipeline/release-tool/privo_release/release.py` (`SDK_RELEASES`), which also creates the GitHub release. No repository under `Privo/` compiles it; the consumers are external integrator apps, so the public API surface is the contract and every public class, function, enum case and data class constructor is part of it. The SDK calls agegate-svc (`age-gate/settings`, `birthdate`, `status`, `recheck`, `link-user`), ageverification-svc (`age-verification?verification_identifier`), common-svc (`storage/put`, `storage/{key}`, `metrics`, `{service_identifier}/user-sessions`) and the auth service (`/authorize?client_id=mobile`, `api/v1.0/privo/login/token`, `api/v1.0/fp`, `api/v1.0/info/svc`). The interactive flows load the web apps `p3-age-gate-ui`, `p3-age-verification-ui` and `p3-verification-ui` from `Privo/privo3-ui` in a `WebView` dialog and exchange state with them through common-svc temporary storage ids in the URL. Birth dates, ages, country codes, user identifiers, nicknames, first names, emails, phone numbers, a device fingerprint derived from `ANDROID_ID`, and the login JWT pass through it; the JWT, the device identifier and the age gate ids are kept in the `SharedPreferences` file `privo-preferences`, which `PrivoBackupAgent` ships to Android backup.

## Repository map

- `sdk/build.gradle`: AGP 7.0.1, Kotlin 1.5.21, compileSdk 36, minSdk 23, jvmTarget 1.8, Moshi codegen through kapt. The `maven-publish` block says `0.0.2`; JitPack builds from the git tag and ignores it.
- `sdk/src/main/java/com/privo/sdk/`: public entry points `Privo`, `PrivoAgeGate`, `PrivoAgeVerification`, `PrivoAuth`, `PrivoVerification`, and `Configuration` with one URL block per `EnvironmentType`.
- `sdk/src/main/java/com/privo/sdk/model/`: wire records and public types. Request and response classes mirror Scala TOs in `Privo/privo3-agegate` and `Privo/privo3-age-verification`; store records mirror TypeScript interfaces in `Privo/privo3-ui`.
- `sdk/src/main/java/com/privo/sdk/api/Rest.kt`: the only HTTP client, OkHttp with a single Moshi instance and all enum adapters.
- `sdk/src/main/java/com/privo/sdk/internal/`: `PrivoInternal` singleton, `PrivoWebViewDialog` and `PrivoWebViewClient` with the finish criteria, `JsPrivoInterface` JavaScript bridge, `PrivoChromeTab`, `PrivoBackupAgent`, `AgeSettingsInternal` settings cache, `age/gate/` with `AgeGateInternal`, `AgeGateStorage` and `AgeGateHelpers`.
- `sdk/src/main/java/com/privo/sdk/extensions/`: internal to public event and status mappers.
- `sdk/src/main/AndroidManifest.xml`: `INTERNET`, `ACCESS_NETWORK_STATE`, `allowBackup`, the backup agent and a `FileProvider` on `${applicationId}.provider`, all merged into the host app.
- `sdk/src/test/` and `sdk/src/androidTest/`: the two generated example tests only; the repository has no real tests and no CI configuration.

## Conventions

- Error handling: network calls are fire and forget with a completion lambda. `Rest.getMoshiCallback` turns transport errors, undecodable bodies and non-2xx responses into `completion(null)` after `handleError`; only `processBirthDate` and `processRecheck` read the `CustomRestError` code, and `2016` (`RequireAgeEstimationToken` in `privo3-agegate/api/src/main/scala/com/privo/agegate/api/enum/AgeGateErrorCodeEnum.scala`) switches to the age estimation page. Input validation throws the exceptions in `model/PrivoExceptions.kt`, listed in `@Throws` on `PrivoAgeGate`.
- Threading: `Rest` enqueues on OkHttp and posts completions to the main looper through `runOnMainThread`; `customErrorCompletion` runs on the OkHttp thread. `PrivoWebViewDialog.show` and `hide` post to the main looper. Integrator completions are expected on the main thread.
- Data access: nothing local beyond `SharedPreferences` `privo-preferences`. Keys are `privo-token-<env>` from `Configuration.tokenStorageKey`, `PrivoFpId-<EnvironmentType>` and `AgeGateStoredEntity-<EnvironmentType>` from `AgeGateStorage`, `AgeVerificationEvent-<userIdentifier>` from `AgeVerificationInternal`, `privo-device-identifier` from `DeviceIdentifier`.
- Configuration: `PrivoSettings(serviceIdentifier, envType, apiKey)` from the integrator, everything else from `Configuration`. All six `EnvironmentType` blocks carry the same eleven fields; a new field must be added to all six. No secret belongs in the repository; `apiKey` is the partner's and is forwarded inside `VerificationConfig` to temporary storage.
- Logging: tag `PRIVO Android SDK`, `Log.e` only. `Rest.handleError` also posts `e.toString()` to `/metrics` as `AnalyticEvent` with `svc = 63` and `event = 299`; `63` is `PrivoAndroidSDK` in `privo3-rs/common_model/src/model/core_enums.rs`, the iOS SDK uses `62`. `AgeGateInternal.linkUser` posts the full stored entity set, user identifiers and nicknames included, as `AgeGateLinkWarning`. Tokens, birth dates, names, emails and phone numbers must not be added to either channel.
- JSON: Moshi with `@JsonClass(generateAdapter = true)`; wire names are the Kotlin property names, so `UserLimitsTO` and `UserSessionRequest` spell snake case in the property itself. Integer enums `AgeGateStatusTO`, `AgeGateAction`, `VerificationMethodType`, `VerificationOutcome` and string enums `AgeGateStatus`, `AgeGateStatusInternal`, `AgeVerificationStatus`, `LimitType` use hand-written adapters that do `values().first { }`; an unknown value throws and becomes `completion(null)`.
- Build flags: none. No ktlint, detekt or Android lint configuration, `minifyEnabled false`, `consumer-rules.pro` empty, `kotlin.code.style=official` in `gradle.properties`.

## Language checklist

### Kotlin

- Visibility is the API. `Rest`, `PrivoWebViewDialog`, `PrivoChromeTab`, `DeviceIdentifier`, `DeviceFingerprintBuilder`, `PermissionsInternal`, `AgeSettingsInternal`, `PrivoMessageConverter`, `DispatchGroup` and `PrivoBackupAgent` are public today; marking one `internal` or removing a public member is a source break for integrators.
- Data classes `CheckAgeData`, `AgeVerificationProfile`, `UserVerificationProfile`, `PrivoSettings`, `AgeGateEvent` and `AgeVerificationEvent` are constructed and destructured positionally by integrators; a new field goes last with a default, never in the middle.
- `when` over `AgeGateAction` in `AgeGateHelpers.toStatus` and over `AgeGateStatusTO` in `extensions/AgeGateStatus.kt` end in `else -> Undefined`; a new server value silently reads as `Undefined`, so a new case needs an explicit branch.
- `extensions/AgeGateEvent.kt` and `extensions/AgeVerificationEvent.kt` call `AgeGateStatus.valueOf(internal.status)`; every `AgeGateStatusInternal` and `AgeVerificationStatusInternal` name other than `Closed` must also exist in the public enum or the mapper throws at runtime.
- `PrivoInternal.configuration` and `settings` throw `UninitializedPropertyAccessException` before `Privo.initialize`; `AgeSettingsInternal` fetches settings in `init`, so constructing `PrivoAgeGate` before `initialize` crashes. Keep that ordering, do not add more work to constructors.
- `AgeGateHelpers.checkUserData` throws `NotAllowedMultiUserUsageException` inside the `getSettings` callback when settings are not cached; `isAgeCorrect` lets `SimpleDateFormat.parse` throw `ParseException`. Neither reaches the caller's `try`; do not extend that pattern.
- `PrivoWebViewClient.doUpdateVisitedHistory` matches `finishCriteria` by substring and guards with `isFinished`; `onFinish` must stay single-shot and must keep skipping `api/v1.0/redirect?target_url`.
- `AgeGateStorage.storeAgId` adds to a `Set<AgeGateStoredEntity>` keyed on all three fields; a second `agId` for the same user is appended, and `getStoredAgeGateId` returns the first match. Changing the lookup changes which age gate a returning user resumes.
- `DeviceIdentifier.getIdentifier` uses `commit()` and `BackupManager.dataChanged()` on purpose so the id survives reinstall.

## What is blocking in this repository

A finding is blocking when a release should be held for it. In every repository that is:

- An exploitable security weakness: missing or bypassable authentication or authorisation, injection, a private key, credential or token committed or exposed, personal data of a child or parent exposed where it was not before.
- Data loss or corruption, a migration that cannot run safely, or a write that cannot be undone.
- A logic error that breaks a common path under realistic use, or a serious performance regression in one.
- A breaking change to an API, event, message or storage format that another repository, an SDK or a partner consumes.

In this repository, in addition:

- A public class, function, enum case, exception type or data class parameter removed, renamed, reordered or made `internal`, or a default removed from a public parameter.
- A field name, type or integer mapping in `model/IdentificationStatusRecord.kt`, `model/AgeGateResponse.kt`, `model/AgeGateAction.kt`, `model/AgeGateStatus.kt` or `model/AgeGateServiceSettings.kt` that no longer matches the Scala TOs in `Privo/privo3-agegate` and `Privo/privo3-configuration`, or `model/AgeVerificationTO.kt` against `Privo/privo3-age-verification`.
- A change to `CheckAgeStoreData`, `AgeVerificationStoreData`, `VerificationData`, the `privo_*_state_id` and `privo_*_events_id` query keys, the finish criteria strings or the `Android.*` bridge method names that the web apps in `Privo/privo3-ui` do not understand.
- The JWT, the device identifier, a birth date, name, email or phone number written to `Log`, sent in `AnalyticEvent.data`, or stored outside `privo-preferences`.
- A URL in `Configuration` missing from one `EnvironmentType`, or `Prod` pointing at a non-production host.
- A change to `AgeGateInternal.runAgeGate` or `runAgeGateByBirthDay` that lets a `Block`, `AgeEstimationBlock` or `MultiUserBlock` response reach the integrator as `Allowed`, or that skips the `processStatus` resync after `IdentityVerified` and `AgeVerified`.
- `PrivoAuth.getToken` returning an expired token, or `logout` leaving `privo-token-<env>` in place.
- A raised `minSdk`, a changed `jvmTarget`, or a new or upgraded runtime dependency in `sdk/build.gradle` without stating the effect on integrator apps.
- A manifest change that adds a permission, changes the `FileProvider` authority or turns the backup agent on or off for the host app.

Anything else is advisory. The lists are examples, not limits: a finding outside them is still blocking when the reviewer shows a release would be held for it.

## What to skip

- Files listed in `.github/ai-review-excludes.txt`, `.idea/`, the Gradle wrapper and generated Moshi adapters under `build/`.
- Formatting and import order; no formatter is enforced, so only flag it when it hides a defect.
- The commented-out code in `PrivoAuth.showBrowserLogin`, `PrivoWebViewDialog` and `AgeGateInternal.runAgeGate`, and the field-format comments in `model/`.
- `WRITE_EXTERNAL_STORAGE` and `READ_EXTERNAL_STORAGE` in `PermissionsInternal`; they are requested for the camera flows on old API levels and ignored on new ones.
- The `maven-publish` version `0.0.2` and `versionName "1.0.0"`; JitPack uses the tag.
- `DispatchGroup` and `AgeGateExpireEvent`, which are unused; one is annotated `@Deprecated` and the other carries a note to replace it.

## Checks to apply on every PR

- If `model/IdentificationStatusRecord.kt`, `model/AgeGateResponse.kt`, `model/AgeGateAction.kt` or `model/AgeGateStatus.kt` changed: compare field names and integer values with `svc/src/main/scala/com/privo/agegate/http/endpoint/internal/AgeGateEndpoint.scala`, `api/src/main/scala/com/privo/agegate/api/to/req/*.scala`, `api/src/main/scala/com/privo/agegate/api/to/AgeGateStatusTO.scala`, `AgeGateActionTO.scala`, `AgeRecordTO.scala` and `api/src/main/scala/com/privo/agegate/api/enum/*.scala` in `Privo/privo3-agegate`.
- If `model/AgeGateServiceSettings.kt` changed: compare with `api/src/main/scala/com/privo/configuration/rest/models/AgeGateSettingsRestTO.scala` in `Privo/privo3-configuration`; the SDK reads a subset and a server default does not make a Kotlin field optional.
- If `model/AgeGateData.kt`, `model/AgeVerificationStoreData.kt` or `model/VerificationData.kt` changed: compare with `packages/p3-age-gate-ui/src/privo.ts` (`CheckAgeStoreData`, `fpId` and `isNativeIntegration` are mobile only) and `packages/privo-shared/src/helpers/temp-store-constants.ts` in `Privo/privo3-ui`.
- If a `finishCriteria`, `printCriteria` or page name in `AgeGateHelpers.getStatusTargetPage` changed: confirm the route exists in `packages/p3-age-gate-ui/src/App.tsx`, `packages/p3-age-verification-ui/src/App.tsx` or `packages/p3-verification-ui/src/App.tsx` in `Privo/privo3-ui`.
- If `internal/JsPrivoInterface.kt` changed: confirm `packages/privo-shared/src/helpers/mobileSdk.ts` and `nativeShare.ts` in `Privo/privo3-ui` still call the same `Android.*` names with the same arguments.
- If `api/Rest.kt` changed a common-svc or auth call: look up `common_svc/src/route/storage.rs`, `metrics.rs`, `security.rs` and `auth/svc/src/route/public/info.rs` in `Privo/privo3-rs`, and `oauth/svc/src/main/scala/com/privo/oauth/http/endpoint/internal/AuthEndpoint.scala` and `FingerprintEndpoint.scala` in `Privo/privo3-auth`. `info/svc` is marked deprecated in the Rust router.
- If `Configuration.kt` changed: confirm all six `EnvironmentType` blocks have the same fields and that only the hostname suffix differs.
- If a public signature changed: state whether `Privo/privo3-ios-sdk` (`Sources/PrivoSDK/`) exposes the same behaviour, since integrators ship both.
- If `sdk/build.gradle` changed: state the new `minSdk`, `compileSdk`, Kotlin or dependency versions and what an integrator must change.
