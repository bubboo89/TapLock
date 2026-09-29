# Battery optimization exemption: rationale and evidence

Reviewed 2026-09-29. The direct exemption request is retained as an optional,
user-initiated recovery path. This document records the supporting evidence and
its limits; it is not a claim that Google Play has approved this use case.

## Policy boundary

[Android's Doze guidance](https://developer.android.com/training/monitoring-device-state/doze-standby#support_for_other_use_cases)
permits direct exemption requests only for qualifying use cases where power
management adversely affects core functionality. Consent alone does not establish
eligibility. Exemption is partial, not a guarantee that a service will stay alive.
OEM process-management settings and Android's Doze exemption are not interchangeable.

## Evidence supporting retention

- [Issue #5](https://github.com/modelorona/TapLock/issues/5) records the original
  concern: aggressive OEM battery management can stop the accessibility service
  and leave the lock widget nonfunctional. The owner confirmed implementation in
  February 2026. This is a documented project rationale, not a device-specific
  reproduction with logs or a controlled before/after comparison.
- [Issue #10](https://github.com/modelorona/TapLock/issues/10) requests a simpler
  exemption flow. It supports the usability motivation, not proof of necessity.
- TapLock's core function is user-triggered screen locking. Its widget and touch
  zones rely on the accessibility service to perform that action. Loss of this
  service can therefore affect core functionality rather than an incidental task.
- `MainActivity.kt` checks `PowerManager.isIgnoringBatteryOptimizations`, explains
  the recovery option, and launches the request only from the battery button's
  `onClick`. Android presents its own confirmation. If the direct intent fails,
  TapLock opens battery optimization settings. Nothing here silently exempts the
  app, and the permission is not used for polling, background uploads, or analytics.
- `TapLockBatteryOptimizationTest` checks the package URI and intent action
  constants. These are implementation checks, not evidence of battery-related
  service failure or proof of Play policy compliance.

## Evidence still needed for a stronger eligibility claim

Capture an affected device/model, Android/OEM build, TapLock version, power-management
settings, and reproducible idle duration. Compare the same locking action with and
without exemption, retaining service-binding and failure logs. Check that any
improvement is caused by this exemption rather than a separate OEM autostart setting,
re-enabling accessibility, or restarting the app. Avoid recording notification
contents, passwords, or other personal data.

The prior Pixel and emulator gesture tests did not measure prolonged idle service
survival or establish a need for exemption. Do not cite them as proof of eligibility.
Keep the `BatteryLife` inspection visible while that evidence is incomplete; do not
suppress it or represent the absence of a lint build failure as Play approval.
